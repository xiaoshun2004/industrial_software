package com.scut.industrial_software.model.constant;

import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class JwtConstants {
    // 固定密钥字符串（建议放入配置文件或环境变量中管理更安全）
    public static final String SECRET_KEY_STRING = "your-fixed-secret-key-asdfghjkl0202020202";

    // Token 有效时间（毫秒），30分钟
    public static final long EXPIRATION_TIME = 30 * 60 * 1000;

    // 将密钥字符串转换为 SecretKey 对象
    public static final SecretKey SECRET_KEY = new SecretKeySpec(
            SECRET_KEY_STRING.getBytes(),
            SignatureAlgorithm.HS256.getJcaName()
    );

    private JwtConstants() {
        // 防止实例化
    }
}
