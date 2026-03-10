package com.scut.industrial_software.utils;

import com.aliyun.cms20190101.models.DescribeMetricLastRequest;
import com.aliyun.cms20190101.models.DescribeMetricLastResponse;
import com.aliyun.credentials.models.CredentialModel;
import com.aliyun.ecs20140526.Client;
import com.aliyun.ecs20140526.models.DescribeInstancesRequest;
import com.aliyun.ecs20140526.models.DescribeInstancesResponse;
import com.aliyun.ecs20140526.models.DescribeInstancesResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.industrial_software.config.AliyunEcsConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
            config.setEndpoint(aliyunEcsConfig.getEndpoint());

            // 建议添加超时配置
            config.setConnectTimeout(aliyunEcsConfig.getConnectionTimeout());
            config.setReadTimeout(aliyunEcsConfig.getReadTimeout());

            this.client = new Client(config);
            log.info("初始化阿里云ECS客户端成功，endpoint: {}", aliyunEcsConfig.getEndpoint());
        } catch (Exception e) {
            log.error("初始化阿里云ECS客户端失败", e);
            throw new RuntimeException("初始化阿里云ECS客户端失败", e);
        }
    }

    @PostConstruct
    public void initCms() {
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
     * 查询ECS实例列表（基础方法）
     * @param pageNum 页码，从1开始
     * @param pageSize 每页数量，最大100
     * @return 响应对象
     */
    public DescribeInstancesResponse describeInstances(Integer pageNum, Integer pageSize) throws Exception {
        DescribeInstancesRequest request = new DescribeInstancesRequest()
                .setRegionId(aliyunEcsConfig.getRegionId())
                .setPageNumber(pageNum != null ? pageNum : 1)
                .setPageSize(pageSize != null ? pageSize : 10);

        log.info("查询ECS实例列表, pageNum: {}, pageSize: {}",
                request.getPageNumber(), request.getPageSize());


        return client.describeInstances(request);
    }

    public Double getMetricValue(String instanceId, String metricName) throws Exception {
        DescribeMetricLastRequest request = new DescribeMetricLastRequest()
                .setNamespace("acs_ecs_dashboard")
                .setMetricName(metricName)
                .setDimensions("{\"instanceId\":\"" + instanceId + "\"}");

        DescribeMetricLastResponse response = cmsClient.describeMetricLast(request);

        if (response.getBody() != null && response.getBody().getDatapoints() != null) {
            String datapoints = response.getBody().getDatapoints();
            if (datapoints != null && !datapoints.isEmpty()) {
                // 使用 ObjectMapper 解析 JSON 字符串
                JsonNode jsonNode = objectMapper.readTree(datapoints);
                if (jsonNode.isArray() && jsonNode.size() > 0) {
                    JsonNode lastPoint = jsonNode.get(jsonNode.size() - 1);
                    JsonNode avgNode = lastPoint.get("Average");
                    if (avgNode != null && !avgNode.isNull()) {
                        return avgNode.asDouble();
                    }
                }
            }
        }
        return null;
    }

}
