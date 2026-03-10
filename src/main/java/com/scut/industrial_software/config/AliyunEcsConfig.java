package com.scut.industrial_software.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "aliyun.ecs")
public class AliyunEcsConfig {
    /**
     * 阿里云访问密钥ID
     */
    private String accessKeyId;
    /**
     * 阿里云访问密钥
     */
    private String accessKeySecret;
    /**
     * 访问的阿里云ECS地域ID
     */
    private String regionId;
    /**
     * 访问的阿里云ECS地域域名
     */
    private String endpoint;
    /**
     * 访问超时时间
     */
    private int connectionTimeout;
    /**
     * 读取超时
     */
    private int readTimeout;
}
