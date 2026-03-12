package com.scut.industrial_software.utils;

import com.aliyun.cms20190101.models.DescribeMetricListRequest;
import com.aliyun.cms20190101.models.DescribeMetricListResponse;
import com.aliyun.ecs20140526.models.*;
import com.aliyun.ecs20140526.Client;
import com.aliyun.teaopenapi.models.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.industrial_software.config.AliyunEcsConfig;
import com.scut.industrial_software.model.vo.AliEcsSpecificationVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AliyunEcsUtils {

    @Autowired
    private AliyunEcsConfig aliyunEcsConfig;

    private Client client;

    private com.aliyun.cms20190101.Client cmsClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct  // Spring 启动时自动调用
    public void initECS() {
        if (this.client != null) {
            return;
        }
        try {
            com.aliyun.credentials.models.Config credentialConfig =
                    new com.aliyun.credentials.models.Config();
            credentialConfig.setType("access_key");
            credentialConfig.setAccessKeyId(aliyunEcsConfig.getAccessKeyId());
            credentialConfig.setAccessKeySecret(aliyunEcsConfig.getAccessKeySecret());

            com.aliyun.credentials.Client credentialClient =
                    new com.aliyun.credentials.Client(credentialConfig);


            Config config = new Config();
            config.setCredential(credentialClient);  // 使用 credentialClient
            config.setRegionId(aliyunEcsConfig.getRegionId());
            String endpoint = aliyunEcsConfig.getEndpoint();
            if (endpoint != null) {
                endpoint = endpoint.trim(); // 去除可能的首尾空格
                config.setEndpoint(endpoint);
            }
            
            // 建议添加超时配置
            config.setConnectTimeout(aliyunEcsConfig.getConnectionTimeout());
            config.setReadTimeout(aliyunEcsConfig.getReadTimeout());

            this.client = new Client(config);
            log.info("初始化阿里云ECS客户端成功，RegionId={}, Endpoint=[{}], Length={}", 
                    aliyunEcsConfig.getRegionId(), endpoint, (endpoint != null ? endpoint.length() : "null"));
        } catch (Exception e) {
            log.error("初始化阿里云ECS客户端失败", e);
            throw new RuntimeException("初始化阿里云ECS客户端失败", e);
        }
    }

    @PostConstruct
    public void initCms() {
        if (this.cmsClient != null) {
            return;
        }
        try {
            com.aliyun.credentials.models.Config credentialConfig =
                    new com.aliyun.credentials.models.Config();
            credentialConfig.setType("access_key");
            credentialConfig.setAccessKeyId(aliyunEcsConfig.getAccessKeyId());
            credentialConfig.setAccessKeySecret(aliyunEcsConfig.getAccessKeySecret());

            com.aliyun.credentials.Client credentialClient =
                    new com.aliyun.credentials.Client(credentialConfig);

            Config config = new Config();
            config.setCredential(credentialClient);
            config.setEndpoint("metrics.cn-hangzhou.aliyuncs.com");

            this.cmsClient = new com.aliyun.cms20190101.Client(config);
            log.info("初始化阿里云云监控客户端成功");
        } catch (Exception e) {
            log.error("初始化云监控客户端失败", e);
            throw new RuntimeException("初始化云监控客户端失败", e);
        }
    }

    /**
     * 查询所有ECS实例（自动处理分页）
     * @return 所有实例列表
     */
    public List<DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance> describeAllInstances() throws Exception {
        List<DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance> allInstances = new ArrayList<>();
        int pageNum = 1;
        int pageSize = 50; // 单页最大通常为100，保守取50
        int totalCount = 0;

        do {
            DescribeInstancesRequest request = new DescribeInstancesRequest()
                    .setRegionId(aliyunEcsConfig.getRegionId())
                    .setPageNumber(pageNum)
                    .setPageSize(pageSize);

            log.info("正在拉取第 {} 页 ECS 实例...", pageNum);
            DescribeInstancesResponse response = client.describeInstances(request);

            if (response.getBody() != null && response.getBody().getInstances() != null) {
                List<DescribeInstancesResponseBody.DescribeInstancesResponseBodyInstancesInstance> pageInstances =
                        response.getBody().getInstances().getInstance();
                if (pageInstances != null && !pageInstances.isEmpty()) {
                    allInstances.addAll(pageInstances);
                }
                totalCount = response.getBody().getTotalCount() != null ? response.getBody().getTotalCount() : 0;
            } else {
                break;
            }
            pageNum++;
        } while (allInstances.size() < totalCount);

        log.info("成功拉取所有 ECS 实例，共 {} 台", allInstances.size());
        return allInstances;
    }

    /**
     * 批量获取实例监控数据 (优化 N+1 问题)
     * @param instanceIds 实例ID列表
     * @param metricName 指标名称 (CPUUtilization, memory_usedutilization)
     * @return Map<InstanceId, Value>
     */
    public Map<String, Double> getBatchMetricValues(List<String> instanceIds, String metricName) throws Exception {
        Map<String, Double> result = new HashMap<>();
        if (instanceIds == null || instanceIds.isEmpty()) {
            return result;
        }

        // 云监控 API 限制单次查询的 Dimensions 不能过多，建议分批处理（例如每批 10-20 个）
        int batchSize = 10;
        for (int i = 0; i < instanceIds.size(); i += batchSize) {
            List<String> subList = instanceIds.subList(i, Math.min(i + batchSize, instanceIds.size()));
            
            // 构建 Dimensions 数组字符串: [{"instanceId":"i-1"}, {"instanceId":"i-2"}]
            List<Map<String, String>> dimensionsList = subList.stream()
                    .map(id -> Collections.singletonMap("instanceId", id))
                    .collect(Collectors.toList());
            String dimensionsJson = objectMapper.writeValueAsString(dimensionsList);

            DescribeMetricListRequest request = new DescribeMetricListRequest()
                    .setNamespace("acs_ecs_dashboard")
                    .setMetricName(metricName)
                    .setDimensions(dimensionsJson)
                    .setPeriod("60") // 监控周期 60s
                    .setLength("1"); // 只取最新的一条数据

            try {
                DescribeMetricListResponse response = cmsClient.describeMetricList(request);
                if (response.getBody() != null && response.getBody().getDatapoints() != null) {
                    String datapoints = response.getBody().getDatapoints();
                    // 打印原始数据，方便调试
                    log.info("监控指标 {} 返回的原始数据: {}", metricName, datapoints);
                    
                    if (datapoints != null && !datapoints.isEmpty()) {
                        JsonNode jsonNode = objectMapper.readTree(datapoints);
                        if (jsonNode.isArray()) {
                            for (JsonNode point : jsonNode) {
                                String instId = point.get("instanceId").asText();
                                JsonNode avgNode = point.get("Average"); // 通常取平均值
                                if (avgNode != null && !avgNode.isNull()) {
                                    result.put(instId, avgNode.asDouble());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("批量拉取监控指标失败: metric={}, instances={}", metricName, subList, e);
                // 不抛出异常，避免影响整个同步流程，只记录日志
            }
        }
        return result;
    }


    /**
     * 获取实例状态
     * @param instanceId 实例ID
     * @return 状态字符串 (Running, Stopped, etc.)
     */
    public String getInstanceStatus(String instanceId) throws Exception {
        DescribeInstancesRequest request = new DescribeInstancesRequest()
                .setRegionId(aliyunEcsConfig.getRegionId())
                .setInstanceIds("[\"" + instanceId + "\"]");
        DescribeInstancesResponse response = client.describeInstances(request);
        
        if (response.getBody() != null && 
            response.getBody().getInstances() != null && 
            !response.getBody().getInstances().getInstance().isEmpty()) {
            return response.getBody().getInstances().getInstance().get(0).getStatus();
        }
        return "Unknown";
    }

    /**
     * 获取指定实例的可变更规格（升配/降配）
     * @param instanceId 实例ID
     * @return 规格列表
     */
    public List<AliEcsSpecificationVO> getAvailableModifications(String instanceId) throws Exception {
        // 1. 获取当前实例详情
        DescribeInstancesRequest describeRequest = new DescribeInstancesRequest()
                .setRegionId(aliyunEcsConfig.getRegionId())
                .setInstanceIds("[\"" + instanceId + "\"]");
        DescribeInstancesResponse describeResponse = client.describeInstances(describeRequest);
        
        if (describeResponse.getBody() == null || 
            describeResponse.getBody().getInstances() == null || 
            describeResponse.getBody().getInstances().getInstance().isEmpty()) {
            throw new RuntimeException("实例不存在: " + instanceId);
        }
        
        var currentInstance = describeResponse.getBody().getInstances().getInstance().get(0);
        String currentSpec = currentInstance.getInstanceType();
        String currentInstanceChargeType = currentInstance.getInstanceChargeType();

        Set<String> availableSpecs = new HashSet<>();

        // 策略1: 尝试 DescribeResourcesModification
        try {
            DescribeResourcesModificationRequest modRequest = new DescribeResourcesModificationRequest()
                    .setRegionId(aliyunEcsConfig.getRegionId())
                    .setResourceId(instanceId)
                    .setDestinationResource("InstanceType")
                    .setOperationType("Upgrade");
            
            DescribeResourcesModificationResponse modResponse = client.describeResourcesModification(modRequest);
            
            if (modResponse.getBody() != null && modResponse.getBody().getAvailableZones() != null) {
                for (var zone : modResponse.getBody().getAvailableZones().getAvailableZone()) {
                    if (zone.getAvailableResources() != null) {
                        for (var res : zone.getAvailableResources().getAvailableResource()) {
                            if (res.getSupportedResources() != null) {
                                for (var support : res.getSupportedResources().getSupportedResource()) {
                                    if ("Available".equalsIgnoreCase(support.getStatus())) {
                                        availableSpecs.add(support.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("DescribeResourcesModification 调用失败或无结果，尝试备用方案: {}", e.getMessage());
        }

        // 策略2: 如果策略1为空，且是按量付费实例，尝试 DescribeAvailableResource
        if (availableSpecs.isEmpty()) {
            DescribeAvailableResourceRequest availRequest = new DescribeAvailableResourceRequest()
                    .setRegionId(currentInstance.getRegionId())
                    .setZoneId(currentInstance.getZoneId())
                    .setDestinationResource("InstanceType")
                    .setInstanceChargeType(currentInstanceChargeType)
                    .setResourceType("instance");

            try {
                DescribeAvailableResourceResponse availResponse = client.describeAvailableResource(availRequest);
                
                if (availResponse.getBody() != null && availResponse.getBody().getAvailableZones() != null) {
                    for (var zone : availResponse.getBody().getAvailableZones().getAvailableZone()) {
                        if (zone.getAvailableResources() != null) {
                            for (var res : zone.getAvailableResources().getAvailableResource()) {
                                if (res.getSupportedResources() != null) {
                                    for (var support : res.getSupportedResources().getSupportedResource()) {
                                        if ("Available".equalsIgnoreCase(support.getStatus())) {
                                            availableSpecs.add(support.getValue());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("查询可用区库存失败", e);
            }
        }
        
        if (availableSpecs.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 批量查询规格详情（CPU/内存）
        List<AliEcsSpecificationVO> result = new ArrayList<>();
        List<String> allSpecs = new ArrayList<>(availableSpecs);
        int batchSize = 10;
        
        for (int i = 0; i < allSpecs.size(); i += batchSize) {
            List<String> subList = allSpecs.subList(i, Math.min(i + batchSize, allSpecs.size()));
            DescribeInstanceTypesRequest typeRequest = new DescribeInstanceTypesRequest()
                    .setInstanceTypes(subList);
            
            try {
                DescribeInstanceTypesResponse typeResponse = client.describeInstanceTypes(typeRequest);
                if (typeResponse.getBody() != null && typeResponse.getBody().getInstanceTypes() != null) {
                    for (var typeInfo : typeResponse.getBody().getInstanceTypes().getInstanceType()) {
                        String specId = typeInfo.getInstanceTypeId();
                        // 排除当前规格
                        if (specId.equals(currentSpec)) continue;

                        AliEcsSpecificationVO vo = new AliEcsSpecificationVO()
                                .setSpecification(specId)
                                .setCpuCore(typeInfo.getCpuCoreCount())
                                .setMemory(typeInfo.getMemorySize().intValue());
                        
                        result.add(vo);
                    }
                }
            } catch (Exception e) {
                log.error("查询规格详情失败: {}", subList, e);
            }
        }
        
        // 5. 排序：按 CPU -> 内存 升序
        result.sort((o1, o2) -> {
            if (!o1.getCpuCore().equals(o2.getCpuCore())) {
                return o1.getCpuCore() - o2.getCpuCore();
            }
            return o1.getMemory() - o2.getMemory();
        });
        
        return result;
    }



    /**
     * 修改实例规格（变配）
     * @param instanceId 实例ID
     * @param targetSpecification 目标规格 (如 ecs.g6.xlarge)
     */
    public void modifyInstanceSpec(String instanceId, String targetSpecification) throws Exception {
        // 1. 创建变配请求
        // 注意：ModifyInstanceSpec 接口通常用于按量付费实例的变配，或者包年包月实例的临时升级
        // 对于包年包月实例的续费升降配，可能需要 ModifyPrepayInstanceSpec 或其他接口
        // 这里以最通用的 ModifyInstanceSpec 为例，并允许系统盘随规格适配
        
        ModifyInstanceSpecRequest request = new ModifyInstanceSpecRequest()
                .setInstanceId(instanceId)
                .setInstanceType(targetSpecification);
                // .setAllowFilterSystemDisk(true); // 允许系统根据规格自动筛选系统盘（兼容性更好）

        // 2. 发起请求
        log.info("正在将实例 {} 变配为 {}", instanceId, targetSpecification);
        ModifyInstanceSpecResponse response = client.modifyInstanceSpec(request);
        
        log.info("变配请求已提交，RequestId: {}", response.getBody().getRequestId());
    }
    public List<AliEcsSpecificationVO> getSameFamilySpecifications(String currentSpecification) throws Exception {
        // 1. 从当前规格提取规格族（保留完整格式，如 e-c1m2）
        String targetFamily = getInstanceTypeFamily(currentSpecification);

        if (targetFamily == null || targetFamily.isEmpty()) {
            throw new RuntimeException("无法识别规格族：" + currentSpecification);
        }

        log.info("当前规格：{}, 规格族：{}", currentSpecification, targetFamily);

        // 2. 不指定规格族，获取所有规格后过滤
        DescribeInstanceTypesRequest request = new DescribeInstanceTypesRequest();

        DescribeInstanceTypesResponse response = client.describeInstanceTypes(request);

        List<AliEcsSpecificationVO> result = new ArrayList<>();

        if (response.getBody() != null &&
                response.getBody().getInstanceTypes() != null &&
                response.getBody().getInstanceTypes().getInstanceType() != null) {

            // ⭐ 获取总规格数
            int totalCount = response.getBody().getInstanceTypes().getInstanceType().size();
            log.info("获取到总规格数：{}", totalCount);

            // ⭐ 遍历 getInstanceType() 返回的 List
            for (var typeInfo : response.getBody().getInstanceTypes().getInstanceType()) {
                String typeId = typeInfo.getInstanceTypeId();
                String typeFamily = getInstanceTypeFamily(typeId);

                // ⭐ 过滤出目标规格族
                if (targetFamily.equals(typeFamily)) {
                    // 排除当前规格
                    if (currentSpecification.equals(typeId)) {
                        continue;
                    }

                    AliEcsSpecificationVO vo = new AliEcsSpecificationVO();
                    vo.setSpecification(typeId);
                    vo.setCpuCore(typeInfo.getCpuCoreCount());
                    vo.setMemory(typeInfo.getMemorySize().intValue());
                    result.add(vo);

                    log.info("添加规格：{} - {}核{}GB", typeId, vo.getCpuCore(), vo.getMemory());
                }
            }
        }

        // 3. 按 CPU 和内存排序
        result.sort(Comparator.comparingInt(AliEcsSpecificationVO::getCpuCore).thenComparingInt(AliEcsSpecificationVO::getMemory));

        log.info("规格族 {} 下的可用规格数量：{}", targetFamily, result.size());
        return result;
    }

    /**
     * 从实例规格中提取规格族（保留完整格式）
     * 如：ecs.e-c1m2.large → e-c1m2
     *     ecs.g6.xlarge → g6
     */
    private String getInstanceTypeFamily(String instanceType) {
        if (instanceType == null || instanceType.isEmpty()) {
            return null;
        }

        // 格式：ecs.<family>.<size>
        String[] parts = instanceType.split("\\.");
        if (parts.length >= 2) {
            return parts[1];  // 直接返回，不拆分连字符
        }

        return null;
    }

}
