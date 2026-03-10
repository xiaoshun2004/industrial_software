package com.scut.industrial_software.service.impl;

import com.aliyun.ecs20140526.models.DescribeInstancesResponseBody;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MonitorServerServiceImpl extends ServiceImpl<MonitorServerMapper, Server> implements IMonitorServerService{

    @Autowired
    private AliyunEcsUtils aliyunEcsUtils;

    private final String CPU_UTILIZATION = "CPUUtilization";

    private final String MEMORY_UTILIZATION = "memory_usedutilization";

    @Override
    public ApiResult<?> getServerPage(MonitorServersPageRequestDTO requestDTO) {
        Page<Server> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<Server> serverPage = baseMapper.selectServerPage(page, requestDTO.getKeyword(), requestDTO.getStatus(), requestDTO.getType());

        List<Server> servers = serverPage.getRecords();

        Map<String,Object> result = new HashMap<>();

        result.put("records", servers);
        result.put("total", serverPage.getTotal());
        result.put("size", serverPage.getSize());
        result.put("current", serverPage.getCurrent());
        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> adjustServerResources(String serverId, Integer cpuCores, Integer memory) {
        Server server = this.getById(serverId);
        if (server == null) {
            return ApiResult.failed("服务器不存在");
        }

        server.setCpuCores(cpuCores);
        server.setMemory(memory);
        boolean result = this.updateById(server);
        if(!result){
            return ApiResult.failed("更新失败");
        } else{
            return ApiResult.success(true, "更新成功");
        }
    }

    /**
     * 同步数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> synchronizeDatabase() {

        log.info("开始同步数据库");

        //初始化ECS客户端
        aliyunEcsUtils.initECS();
        aliyunEcsUtils.initCms();

        //获取所有实例
        try{
            var response = aliyunEcsUtils.describeInstances(1, 100);
            log.info("获取实例列表成功, 共{}个实例", response.getBody().getTotalCount());

            List<DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance> instances = response.getBody().getInstances().getInstance();

            for (DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance instance : instances) {
                String instanceName = instance.getInstanceName();
                log.info("开始处理实例: {}", instanceName);
                Server existingServer = getBaseMapper().selectOne(
                        new LambdaQueryWrapper<Server>().eq(Server::getName, instanceName)
                );
                Server server = convertEcsInstanceToserver(instance);

                if(existingServer != null){
                    log.info("实例已存在, 更新实例信息");

                    server.setId(existingServer.getId());

                    getBaseMapper().updateById(server);
                    log.info("更新服务器：{} - 状态：{}", server.getName(), server.getStatus());
                } else{
                    log.info("实例不存在, 添加实例");

                    getBaseMapper().insert(server);

                    log.info("添加服务器：{} - 状态：{}", server.getName(), server.getStatus());

                }

            }

        } catch (Exception e){
            return ApiResult.failed("同步数据库失败");
        }


        return ApiResult.success("同步数据库成功");
    }

    private Server convertEcsInstanceToserver(DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance instance) throws Exception {
        Server server = new Server();

        server.setName(instance.getInstanceName());

        server.setIp(getServerIp(instance));

        server.setSpecification(instance.getInstanceType());

        server.setStatus(instance.getStatus());

        server.setCpuCores(instance.getCpu());

        server.setMemory(instance.getMemory());

        Double cpuUsage = aliyunEcsUtils.getMetricValue(instance.getInstanceId(), CPU_UTILIZATION);
        Double memoryUsage = aliyunEcsUtils.getMetricValue(instance.getInstanceId(), MEMORY_UTILIZATION);
        server.setCpuUsage(cpuUsage != null ? String.format("%.2f", cpuUsage) : "N/A");
        server.setMemoryUsage(memoryUsage != null ? String.format("%.2f", memoryUsage) : "N/A");


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
