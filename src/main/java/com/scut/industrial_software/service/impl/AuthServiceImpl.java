package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.dto.UserLoginDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.vo.LoginResponseVO;
import com.scut.industrial_software.service.IAuthService;
import com.scut.industrial_software.service.IModUsersService;
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

    @Override
    public LoginResponseVO login(UserLoginDTO loginDTO) {
        // 1. 验证用户
        ModUsers user = modUsersService.login(loginDTO);
        if (user == null) {
            return null;
        }

        // 2. 生成Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getUserId());
        claims.put("name", user.getUsername());
        String token = JwtUtils.generateToken(claims);

        // 3. 存入Redis
        redisTemplate.opsForValue().set(
                RedisConstants.USER_TOKEN_KEY_PREFIX + user.getUsername() + ":token",
                token,
                RedisConstants.TOKEN_TTL,
                TimeUnit.MINUTES
        );

        // 4. 构建响应对象
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