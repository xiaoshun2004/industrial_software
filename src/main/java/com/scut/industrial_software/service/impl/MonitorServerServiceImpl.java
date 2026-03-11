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
        
        // 临时方案：先尝试用 server.getName() 作为关键字去阿里云搜一下，获取真正的 InstanceId
        // 更推荐的方案是：修改 Server 实体，增加 instanceId 字段，同步时保存。
        
        try {
            var allInstances = aliyunEcsUtils.describeAllInstances();
            String realInstanceId = null;
            for (var inst : allInstances) {
                if (inst.getInstanceName() != null && inst.getInstanceName().equals(server.getName())) {
                    realInstanceId = inst.getInstanceId();
                    break;
                }
            }
            
            if (realInstanceId == null) {
                return ApiResult.failed("未在阿里云找到对应的 ECS 实例，请先同步数据库");
            }
            
            // 3. 调用工具类获取可变更规格
            var specs = aliyunEcsUtils.getAvailableModifications(realInstanceId);
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
        Server server = this.getById(serverId);
        if (server == null) {
            return ApiResult.failed("服务器不存在");
        }

        // 1. 获取真实的阿里云实例ID (同样需要反查)
        String realInstanceId = null;
        try {
            var allInstances = aliyunEcsUtils.describeAllInstances();
            for (var inst : allInstances) {
                if (inst.getInstanceName() != null && inst.getInstanceName().equals(server.getName())) {
                    realInstanceId = inst.getInstanceId();
                    break;
                }
            }
        } catch (Exception e) {
            log.error("查询实例信息失败", e);
            return ApiResult.failed("查询实例信息失败");
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
        } catch (Exception e) {
            log.error("实例变配失败", e);
            return ApiResult.failed("实例变配失败: " + e.getMessage());
        }

        // 3. 更新本地数据库记录 (注意：实际生效可能需要重启实例，这里先更新状态为 '修改中' 或直接更新规格)
        // 建议：变配后实例状态通常会变，最好让定时任务去同步，或者这里手动触发一次同步
        // 这里简单更新一下数据库的规格字段，并记录日志
        server.setSpecification(specification);
        boolean result = this.updateById(server);
        
        if(!result){
            return ApiResult.failed("阿里云变配请求已提交，但本地数据库更新失败");
        } else{
            // 可以在这里触发一次异步同步
            // synchronizeDatabase(); 
            return ApiResult.success(true, "变配请求已提交，请稍后刷新查看最新状态");
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
                
                log.info("正在处理实例: {} ({})", instanceName, instanceId);
                
                // 从批量查询结果中获取监控值
                Double cpuUsage = cpuMap.getOrDefault(instanceId, 0.0);
                Double memUsage = memMap.getOrDefault(instanceId, 0.0);

                Server server = convertEcsInstanceToserver(instance, cpuUsage, memUsage);
                
                Server existingServer = getBaseMapper().selectOne(
                        new LambdaQueryWrapper<Server>().eq(Server::getName, instanceName)
                );

                if(existingServer != null){
                    server.setId(existingServer.getId());
                    getBaseMapper().updateById(server);
                    log.debug("更新服务器：{}", server.getName());
                } else{
                    getBaseMapper().insert(server);
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
        server.setName(instance.getInstanceName());
        server.setIp(getServerIp(instance));
        server.setSpecification(instance.getInstanceType());
        server.setStatus(instance.getStatus());
        server.setCpuCores(instance.getCpu());
        server.setMemory(instance.getMemory());

        // 格式化监控数据
        server.setCpuUsage(cpuUsage > 0 ? String.format("%.2f", cpuUsage) : "N/A");
        server.setMemoryUsage(memoryUsage > 0 ? String.format("%.2f", memoryUsage) : "N/A");

        return server;
    }

    private String getServerIp(DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance instance){
        // 优先获取公网 IP
        if (instance.getPublicIpAddress() != null &&
                instance.getPublicIpAddress().getIpAddress() != null &&
                !instance.getPublicIpAddress().getIpAddress().isEmpty()) {
            return instance.getPublicIpAddress().getIpAddress().get(0);
        }

        // 其次获取 EIP
        if (instance.getEipAddress() != null &&
                instance.getEipAddress().getIpAddress() != null) {
            return instance.getEipAddress().getIpAddress();
        }

        // 最后获取私网 IP
        if (instance.getVpcAttributes() != null &&
                instance.getVpcAttributes().getPrivateIpAddress() != null &&
                instance.getVpcAttributes().getPrivateIpAddress().getIpAddress() != null &&
                !instance.getVpcAttributes().getPrivateIpAddress().getIpAddress().isEmpty()) {
            return instance.getVpcAttributes().getPrivateIpAddress().getIpAddress().get(0);
        }

        if (instance.getInnerIpAddress() != null &&
                instance.getInnerIpAddress().getIpAddress() != null &&
                !instance.getInnerIpAddress().getIpAddress().isEmpty()) {
            return instance.getInnerIpAddress().getIpAddress().get(0);
        }

        return "N/A";
    }

}
