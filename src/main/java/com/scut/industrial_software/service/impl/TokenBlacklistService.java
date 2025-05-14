package com.scut.industrial_software.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.scut.industrial_software.model.constant.RedisConstants.TOKEN_TTL;

@Service
public class TokenBlacklistService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";  // Redis 中存储黑名单的前缀

    // 将 Token 加入黑名单，永久有效（不设置过期时间）
    public void addToBlacklist(String token) {
        redisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + token, "1",TOKEN_TTL, TimeUnit.MINUTES);  // 不设置过期时间，使其永久失效
    }

    // 检查 Token 是否在黑名单中
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + token);  // 检查 Redis 中是否存在该 Token
    }

    // 从黑名单中删除 Token（例如，用户重新登录时）
    public void removeFromBlacklist(String token) {
        redisTemplate.delete(BLACKLIST_KEY_PREFIX + token);  // 从 Redis 中移除该 Token
    }
}