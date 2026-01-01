package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.model.constant.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.scut.industrial_software.model.constant.RedisConstants.TOKEN_TTL;

@Slf4j
@Service
public class TokenBlacklistService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";  // Redis 中存储黑名单的前缀

    /**
     * 将 Token 加入黑名单
     * @param token 要加入黑名单的token
     */
    public void addToBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("尝试将空token添加到黑名单");
            return;
        }
        
        try {
            redisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + token, "blacklisted", TOKEN_TTL, TimeUnit.MINUTES);
            log.debug("Token已加入黑名单: {}", token.substring(Math.max(0, token.length() - 8))); // 只记录后8位
        } catch (Exception e) {
            log.error("将token加入黑名单时发生异常", e);
        }
    }
    
    /**
     * 将旧设备的Token踢下线（加入黑名单）
     * @param token 旧设备的token
     * @param reason 踢下线的原因
     */
    public void kickOutDevice(String token, String reason) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        try {
            String value = reason != null ? reason : "kicked_out_by_new_login";
            redisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + token, value, RedisConstants.BLACKLIST_TOKEN_TTL, TimeUnit.MINUTES);
            log.info("设备已被踢下线，原因: {}", reason);
        } catch (Exception e) {
            log.error("踢出设备时发生异常", e);
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     * @param token 要检查的token
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        try {
            return redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + token);
        } catch (Exception e) {
            log.error("检查token黑名单状态时发生异常", e);
            return false; // 出现异常时，保守地认为token有效
        }
    }

    /**
     * 从黑名单中删除 Token（例如，用户重新登录时）
     * @param token 要从黑名单删除的token
     */
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        try {
            redisTemplate.delete(BLACKLIST_KEY_PREFIX + token);
            log.debug("Token已从黑名单中移除: {}", token.substring(Math.max(0, token.length() - 8)));
        } catch (Exception e) {
            log.error("从黑名单移除token时发生异常", e);
        }
    }
}