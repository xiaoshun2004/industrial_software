package com.scut.industrial_software.model.constant;

/**
 * License证书配置相关常量
 */
public class LicenseConstants {
    // 证书subject主题
    public static final String LICENSE_SUBJECT = "license";
    // 私钥别称
    public static final String PRIVATE_ALIAS = "privateKey";
    // 私钥库密码
    public static final String KEY_STORE_PASSWORD = "public_password1234";
    // 证书生成路径（在项目根目录下）
    public static final String LICENSE_PATH = "src/main/resources/license/license.lic";
    // 私钥库存储路径（相对于类路径）
    public static final String PRIVATE_KEYS_STORE_PATH = "src/main/resources/license/privateKeys.keystore";
}
