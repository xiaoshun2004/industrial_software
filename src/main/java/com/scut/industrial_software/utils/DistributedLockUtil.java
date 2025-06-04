package com.scut.industrial_software.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 * 基于Redis实现的分布式锁，支持自动获取、释放和超时机制
 */
@Slf4j
@Component
public class DistributedLockUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 执行带锁的操作
     * @param lockKey 锁的键
     * @param expireTime 锁的过期时间
     * @param timeUnit 时间单位
     * @param action 需要执行的操作
     * @param <T> 返回值类型
     * @return 操作的执行结果
     * @throws RuntimeException 如果获取锁失败
     */
    public <T> T executeWithLock(String lockKey, long expireTime, TimeUnit timeUnit, Supplier<T> action) {
        String lockValue = UUID.randomUUID().toString();
        
        try {
            // 尝试获取锁
            boolean lockAcquired = acquireLock(lockKey, lockValue, expireTime, timeUnit);
            if (!lockAcquired) {
                throw new RuntimeException("获取分布式锁失败，请稍后重试");
            }
            
            log.info("成功获取分布式锁: {}", lockKey);
            
            // 执行业务逻辑
            return action.get();
            
        } finally {
            // 释放锁
            releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 尝试执行带锁的操作，如果获取锁失败则返回默认值
     * @param lockKey 锁的键
     * @param expireTime 锁的过期时间
     * @param timeUnit 时间单位
     * @param action 需要执行的操作
     * @param defaultValue 获取锁失败时的默认返回值
     * @param <T> 返回值类型
     * @return 操作的执行结果或默认值
     */
    public <T> T tryExecuteWithLock(String lockKey, long expireTime, TimeUnit timeUnit, 
                                   Supplier<T> action, T defaultValue) {
        String lockValue = UUID.randomUUID().toString();
        
        try {
            // 尝试获取锁
            boolean lockAcquired = acquireLock(lockKey, lockValue, expireTime, timeUnit);
            if (!lockAcquired) {
                log.warn("获取分布式锁失败: {}", lockKey);
                return defaultValue;
            }
            
            log.debug("成功获取分布式锁: {}", lockKey);
            
            // 执行业务逻辑
            return action.get();
            
        } finally {
            // 释放锁
            releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 获取分布式锁
     * @param lockKey 锁的键
     * @param lockValue 锁的值（用于安全释放）
     * @param expireTime 锁的过期时间
     * @param timeUnit 时间单位
     * @return 是否成功获取锁
     */
    private boolean acquireLock(String lockKey, String lockValue, long expireTime, TimeUnit timeUnit) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, timeUnit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("获取分布式锁异常: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁（使用Lua脚本确保原子性）
     * @param lockKey 锁的键
     * @param lockValue 锁的值
     */
    private void releaseLock(String lockKey, String lockValue) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(LuaScriptConstants.RELEASE_LOCK_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, Arrays.asList(lockKey), lockValue);
            
            if (result != null && result == 1) {
                log.debug("成功释放分布式锁: {}", lockKey);
            } else {
                log.warn("释放分布式锁失败，可能已过期: {}", lockKey);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常: {}", lockKey, e);
        }
    }
} 