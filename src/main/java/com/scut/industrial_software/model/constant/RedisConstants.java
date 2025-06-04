package com.scut.industrial_software.model.constant;

public class RedisConstants {

    // 用户 token 的 Redis 键前缀
    public static final String USER_TOKEN_KEY_PREFIX = "user:token:";

    // 验证码的 Redis 键前缀
    public static final String VERIFY_CODE_PREFIX = "verifyCode:";

    // 用户登录锁的 Redis 键前缀
    public static final String USER_LOGIN_LOCK_PREFIX = "user:login:lock:";

    // token 的过期时间（30分钟）
    public static final long TOKEN_TTL = 30L;

    // 验证码的过期时间（5分钟）
    public static final long VERIFY_CODE_TTL = 5L; // 验证码有效期为 5 分钟

    // 登录锁的过期时间（10秒）
    public static final long LOGIN_LOCK_TTL = 10L; // 登录锁过期时间，防止死锁
}

