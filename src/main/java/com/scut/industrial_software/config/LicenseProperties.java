package com.scut.industrial_software.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * License证书配置属性类，包括证书生成路径、私钥库路径等
 */
@Component
@ConfigurationProperties(prefix = "ptc.license")
@Data
public class LicenseProperties {
    /**
     * 公钥别称
     */
    private String publicAlias;

    /**
     * 访问公钥库的密码
     */
    private String storePass;

    /**
     * 证书生成路径
     */
    private String licensePath;

    /**
     * 密钥库存储路径
     */
    private String publicKeysStorePath;

    /**
     * 密钥别称
     */
    private String privateAlias;

    /**
     * 密钥密码（需要妥善保管，不能让使用者知道）
     */
    private String keyPass;

    /**
     * 密钥库存储路径
     */
    private String privateKeysStorePath;

    /**
     * 是否开启license验证服务器系统信息, 不配置，默认开启
     */
    private Boolean verifySystemSwitch = true;

}
