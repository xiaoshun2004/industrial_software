package com.scut.industrial_software.service.impl;

import com.aliyun.ecs20140526.models.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.MonitorServerMapper;
import com.scut.industrial_software.model.dto.MonitorServersPageRequestDTO;
import com.scut.industrial_software.model.entity.Server;
import com.scut.industrial_software.model.vo.AliEcsSpecificationVO;
import com.scut.industrial_software.service.IMonitorServerService;
import com.scut.industrial_software.utils.AliyunEcsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.aliyun.ecs20140526.models.*;

@Slf4j
@Service
public class MonitorServerServiceImpl extends ServiceImpl<MonitorServerMapper, Server> implements IMonitorServerService{

    @Autowired
    private AliyunEcsUtils aliyunEcsUtils;

    private final String CPU_UTILIZATION = "CPUUtilization";

    // 注意：Windows 实例可能不支持 memory_usedutilization，需确认云监控插件状态
    private final String MEMORY_UTILIZATION = "memory_usedutilization";

    @Override
    public ApiResult<?> getAvailableSpecifications(String serverId) {
        // 1. 根据数据库ID查询服务器实体
        Server server = this.getById(serverId);
        if (server == null) {
            return ApiResult.failed("服务器不存在");
        }
        
        // 优先使用 instanceId，如果为空则降级使用 name (兼容旧数据)
        String realInstanceId = server.getInstanceId();
        
        if (realInstanceId == null || realInstanceId.isEmpty()) {
            try {
                var allInstances = aliyunEcsUtils.describeAllInstances();
                for (var inst : allInstances) {
                    if (inst.getInstanceName() != null && inst.getInstanceName().equals(server.getName())) {
                        realInstanceId = inst.getInstanceId();
                        // 顺便回填数据库
                        server.setInstanceId(realInstanceId);
                        this.updateById(server);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("查询实例列表失败", e);
            }
        }
        
        if (realInstanceId == null) {
            return ApiResult.failed("未在阿里云找到对应的 ECS 实例，请先同步数据");
        }

        try {
            List<AliEcsSpecificationVO> specs = aliyunEcsUtils.getAvailableModifications(realInstanceId);
            return ApiResult.success(specs);
        } catch (Exception e) {
            log.error("获取可变更规格失败", e);
            return ApiResult.failed("获取可变更规格失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<?> getServerPage(MonitorServersPageRequestDTO requestDTO) {
        Page<Server> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<Server> serverPage = baseMapper.selectServerPage(page, requestDTO.getKeyword(), requestDTO.getStatus(), requestDTO.getType());

        List<Server> servers = serverPage.getRecords();

        Map<String, Object> result = new java.util.HashMap<>();

        result.put("records", servers);
        result.put("total", serverPage.getTotal());
        result.put("size", serverPage.getSize());
        result.put("current", serverPage.getCurrent());
        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> adjustServerResources(String serverId, String specification) {
        // 1. 根据数据库ID查询服务器实体
        Server server = this.getById(serverId);
        if (server == null) {
            return ApiResult.failed("服务器不存在");
        }

        String realInstanceId = server.getInstanceId();
        
        // 兼容旧数据：如果没有 instanceId，尝试通过 name 查找
        if (realInstanceId == null || realInstanceId.isEmpty()) {
             try {
                var allInstances = aliyunEcsUtils.describeAllInstances();
                for (var inst : allInstances) {
                    if (inst.getInstanceName() != null && inst.getInstanceName().equals(server.getName())) {
                        realInstanceId = inst.getInstanceId();
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("查询实例列表失败", e);
            }
        }

        if (realInstanceId == null) {
            return ApiResult.failed("未在阿里云找到对应的 ECS 实例，无法执行变配");
        }

        // 2. 检查实例状态
        try {
            String status = aliyunEcsUtils.getInstanceStatus(realInstanceId);
            if (!"Stopped".equalsIgnoreCase(status)) {
                return ApiResult.failed("该状态下无法变配规格，请先停止实例 (当前状态: " + status + ")");
            }
        } catch (Exception e) {
            log.error("获取实例状态失败", e);
            return ApiResult.failed("获取实例状态失败: " + e.getMessage());
        }

        // 3. 调用阿里云接口执行变配
        try {
            aliyunEcsUtils.modifyInstanceSpec(realInstanceId, specification);
            
            // 4. 更新本地数据库记录（可选，也可以等下次同步）
            server.setSpecification(specification);
            this.updateById(server);
            
            return ApiResult.success(true);
        } catch (Exception e) {
            log.error("变配失败", e);
            return ApiResult.failed("变配失败: " + e.getMessage());
        }
    }

    /**
     * 同步数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> synchronizeDatabase() {

        log.info("开始同步数据库");

        // 移除手动 init 调用，改由 Spring @PostConstruct 自动管理

        try {
            // 1. 获取所有实例（自动处理分页）
            List<DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance> instances = 
                    aliyunEcsUtils.describeAllInstances();
            log.info("获取实例列表成功, 共{}个实例", instances.size());

            if (instances.isEmpty()) {
                return ApiResult.success("没有发现 ECS 实例");
            }

            // 2. 批量获取监控数据 (避免 N+1 问题)
            List<String> instanceIds = instances.stream()
                    .map(DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance::getInstanceId)
                    .collect(Collectors.toList());
            
            // 并行获取 CPU 和 内存数据
            Map<String, Double> cpuMap = aliyunEcsUtils.getBatchMetricValues(instanceIds, CPU_UTILIZATION);
            Map<String, Double> memMap = aliyunEcsUtils.getBatchMetricValues(instanceIds, MEMORY_UTILIZATION);
            
            // 提示：如果内存数据为空，说明实例未安装或运行云监控插件
            long missingCount = instanceIds.stream()
                    .filter(id -> !memMap.containsKey(id) || memMap.get(id) == 0.0)
                    .count();
            
            if (missingCount > 0) {
                log.warn("有 {} 台实例未获取到内存监控数据(memory_usedutilization)，请检查云监控插件是否安装运行", missingCount);
            }

            // 3. 遍历处理并更新数据库
            for (DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance instance : instances) {
                String instanceName = instance.getInstanceName();
                String instanceId = instance.getInstanceId();
                String status = instance.getStatus();
                String rawIp = getServerIp(instance);
                
                log.info("同步实例: ID={}, Name={}, Status={}, IP={}", instanceId, instanceName, status, rawIp);
                
                // 从批量查询结果中获取监控值
                Double cpuUsage = cpuMap.getOrDefault(instanceId, 0.0);
                Double memUsage = memMap.getOrDefault(instanceId, 0.0);

                Server server = convertEcsInstanceToserver(instance, cpuUsage, memUsage);
                
                // 优先通过 instanceId 匹配
                Server existingServer = getBaseMapper().selectOne(
                        new LambdaQueryWrapper<Server>().eq(Server::getInstanceId, instanceId)
                );
                
                // 兼容旧数据：如果没有 instanceId，尝试通过 name 匹配
                if (existingServer == null) {
                    existingServer = getBaseMapper().selectOne(
                            new LambdaQueryWrapper<Server>().eq(Server::getName, instanceName)
                    );
                }

                if(existingServer != null){
                    server.setId(existingServer.getId());
                    // 确保 instanceId 也被更新进去
                    if (existingServer.getInstanceId() == null) {
                        server.setInstanceId(instanceId);
                    }
                    this.updateById(server);
                    log.info("更新服务器 [ID={}]：Name={}, Status={}, IP={}", existingServer.getId(), server.getName(), server.getStatus(), server.getIp());
                } else{
                    this.save(server);
                    log.info("新增服务器：{}", server.getName());
                }
            }

        } catch (Exception e){
            log.error("同步数据库过程中发生异常", e);
            return ApiResult.failed("同步数据库失败: " + e.getMessage());
        }

        return ApiResult.success("同步数据库成功");
    }

    private Server convertEcsInstanceToserver(
            DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance instance,
            Double cpuUsage, 
            Double memoryUsage) {
        
        Server server = new Server();
        server.setInstanceId(instance.getInstanceId()); // 新增字段
        server.setName(instance.getInstanceName());
        server.setIp(getServerIp(instance));
        server.setSpecification(instance.getInstanceType());
        server.setStatus(instance.getStatus());
        server.setCpuCores(instance.getCpu());
        
        // 内存单位转换：阿里云 API 返回的是 MB，数据库存的是 GB (或者如果是MB则直接存，但用户要求转GB)
        // 假设 instance.getMemory() 返回的是 Integer 类型的 MB 值
        if (instance.getMemory() != null) {
            // 将 MB 转换为 GB
            server.setMemory(instance.getMemory() / 1024);
        } else {
            server.setMemory(0);
        }

        // 格式化监控数据
        server.setCpuUsage(cpuUsage > 0 ? String.format("%.2f", cpuUsage) : "N/A");
        server.setMemoryUsage(memoryUsage > 0 ? String.format("%.2f", memoryUsage) : "N/A");

        return server;
    }

    private String getServerIp(DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance instance){
        String ip = "N/A";
        
        // 优先获取公网 IP
        if (instance.getPublicIpAddress() != null &&
                instance.getPublicIpAddress().getIpAddress() != null &&
                !instance.getPublicIpAddress().getIpAddress().isEmpty()) {
            ip = instance.getPublicIpAddress().getIpAddress().get(0);
        }

        // 其次获取 EIP (必须不为空字符串)
        if ("N/A".equals(ip) && instance.getEipAddress() != null &&
                instance.getEipAddress().getIpAddress() != null &&
                !instance.getEipAddress().getIpAddress().isEmpty()) {
            ip = instance.getEipAddress().getIpAddress();
        }

        // 获取私网 IP (VPC)
        if ("N/A".equals(ip) && instance.getVpcAttributes() != null &&
                instance.getVpcAttributes().getPrivateIpAddress() != null &&
                instance.getVpcAttributes().getPrivateIpAddress().getIpAddress() != null &&
                !instance.getVpcAttributes().getPrivateIpAddress().getIpAddress().isEmpty()) {
            ip = instance.getVpcAttributes().getPrivateIpAddress().getIpAddress().get(0);
        }

        // 获取内网 IP (经典网络)
        if ("N/A".equals(ip) && instance.getInnerIpAddress() != null &&
                instance.getInnerIpAddress().getIpAddress() != null &&
                !instance.getInnerIpAddress().getIpAddress().isEmpty()) {
            ip = instance.getInnerIpAddress().getIpAddress().get(0);
        }
        
        // 记录调试日志，方便排查
        if ("N/A".equals(ip) || "".equals(ip)) {
            log.warn("实例 {} ({}) 未获取到有效 IP", instance.getInstanceName(), instance.getInstanceId());
        }

        return ip;
    }

}
