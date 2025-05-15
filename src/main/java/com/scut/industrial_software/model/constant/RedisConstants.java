package com.scut.industrial_software.model.constant;

public class RedisConstants {

    // 用户 token 的 Redis 键前缀
    public static final String USER_TOKEN_KEY_PREFIX = "user:token:";

    // 验证码的 Redis 键前缀
    public static final String VERIFY_CODE_PREFIX = "verifyCode:";

    // token 的过期时间（30分钟）
    public static final long TOKEN_TTL = 30L;

    // 验证码的过期时间（5分钟）
    public static final long VERIFY_CODE_TTL = 5L; // 验证码有效期为 5 分钟
}

