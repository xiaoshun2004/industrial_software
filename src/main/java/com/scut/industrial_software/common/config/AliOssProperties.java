package com.scut.industrial_software.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS相关配置
 *
 * 该类用来承接application.yml中 ali.oss.* 前缀下的所有字段。
 * 示例yml配置：
 *
 * ali:
 *   oss:
 *     endpoint: oss-cn-guangzhou.aliyuncs.com
 *     bucket-name: your-bucket-name
 *     access-key-id: YOUR_ACCESS_KEY_ID
 *     access-key-secret: YOUR_ACCESS_KEY_SECRET
 */
@Component
@ConfigurationProperties(prefix = "ali.oss") // 与 application.yml 中的 ali.oss 前缀对应
public class AliOssProperties {

    /**
     * OSS地域节点，如：oss-cn-guangzhou.aliyuncs.com
     */
    private String endpoint;

    /**
     * 需要访问的存储空间（Bucket名称）
     */
    private String bucketName;

    /**
     * 阿里云OSS的Access Key ID
     */
    private String accessKeyId;

    /**
     * 阿里云OSS的Access Key Secret
     */
    private String accessKeySecret;

    // ------ Getter / Setter ------

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
}
