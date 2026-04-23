package com.scut.industrial_software.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "component.download")
@Getter
@Setter
public class ComponentDownloadProperties {

    /**
     * 组件安装包根目录。
     */
    private String sourceRoot = "D:/resources";

    /**
     * 前端可访问的后端公开地址。
     */
    private String publicBaseUrl = "http://localhost:8081";

    /**
     * 单组件下载临时 Token 有效期，单位分钟。
     */
    private Integer tokenExpireMinutes = 10;
}
