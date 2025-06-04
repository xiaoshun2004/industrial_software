package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.dto.UserLoginDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.vo.LoginResponseVO;
import com.scut.industrial_software.service.IAuthService;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.service.impl.TokenBlacklistService;
import com.scut.industrial_software.utils.DistributedLockUtil;
import com.scut.industrial_software.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private IModUsersService modUsersService;

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Autowired
    private DistributedLockUtil distributedLockUtil;

    @Override
    public LoginResponseVO login(UserLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String lockKey = RedisConstants.USER_LOGIN_LOCK_PREFIX + username;
        
        // 使用分布式锁确保同一用户同时只能有一个登录操作
        return distributedLockUtil.executeWithLock(
                lockKey, 
                RedisConstants.LOGIN_LOCK_TTL, 
                TimeUnit.SECONDS,
                () -> executeLoginLogic(loginDTO)
        );
    }
    
    /**
     * 执行实际的登录逻辑
     */
    private LoginResponseVO executeLoginLogic(UserLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        
        // 1. 验证用户凭据
        ModUsers user = modUsersService.login(loginDTO);
        if (user == null) {
            return null;
        }
        
        // 2. 检查是否已有登录会话，如果有则先清理
        String existingTokenKey = RedisConstants.USER_TOKEN_KEY_PREFIX + username + ":token";
        String existingToken = redisTemplate.opsForValue().get(existingTokenKey);
        if (existingToken != null) {
            log.info("用户 {} 已有登录会话，将踢出旧设备", username);
            // 将旧token加入黑名单，立即踢下线
            tokenBlacklistService.kickOutDevice(existingToken, "new_device_login");
        }
        
        // 3. 生成新的Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getUserId());
        claims.put("name", user.getUsername());
        String token = JwtUtils.generateToken(claims);
        
        // 4. 原子性地更新Redis中的token（覆盖旧的）
        redisTemplate.opsForValue().set(
                existingTokenKey,
                token,
                RedisConstants.TOKEN_TTL,
                TimeUnit.MINUTES
        );
        
        log.info("用户 {} 登录成功，生成新token", username);
        
        // 5. 构建响应对象
        return LoginResponseVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .token(token)
                .permission(user.getPermission())
                .phone(user.getPhone())
                .build();
    }

    @Override
    public boolean verifyCode(String key, String code) {
        String verifyKey = RedisConstants.VERIFY_CODE_PREFIX + key;
        String codeInRedis = redisTemplate.opsForValue().get(verifyKey);
        
        if (codeInRedis == null) {
            return false;
        }
        
        boolean isValid = codeInRedis.equalsIgnoreCase(code);
        if (isValid) {
            // 验证通过后删除验证码
            redisTemplate.delete(verifyKey);
        }
        
        return isValid;
    }
} 