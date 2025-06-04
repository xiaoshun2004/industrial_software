package com.scut.industrial_software.utils;

/**
 * Lua脚本常量类
 * 用于存储Redis操作的Lua脚本，确保操作的原子性
 */
public class LuaScriptConstants {
    
    /**
     * 分布式锁释放脚本
     * 只有当锁的值匹配时才能释放锁，确保安全性
     */
    public static final String RELEASE_LOCK_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;
    
    /**
     * Token验证和刷新脚本
     * 如果token匹配则刷新过期时间，原子性操作
     */
    public static final String VALIDATE_AND_REFRESH_TOKEN_SCRIPT = """
            local current_token = redis.call('get', KEYS[1])
            if current_token == ARGV[1] then
                redis.call('expire', KEYS[1], ARGV[2])
                return 1
            else
                return 0
            end
            """;
    
    private LuaScriptConstants() {
        // 防止实例化
    }
} 