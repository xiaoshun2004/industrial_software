package com.scut.industrial_software.controller.User;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.dto.UserLoginDTO;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.vo.LoginResponseVO;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
public class LoginController {

    @Autowired
    private IModUsersService iModUsersService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("auth/jsonLogin")
    public ApiResult<LoginResponseVO> login(@RequestBody UserLoginDTO dto){

        log.info("用户登录，name：{}",dto.getUsername());
        //todo:验证码
        String verifyKey = RedisConstants.VERIFY_CODE_PREFIX + dto.getKey();
        String codeInRedis = redisTemplate.opsForValue().get(verifyKey);
        if (codeInRedis == null) {
            return ApiResult.failed("验证码已过期");
        }
        if (!codeInRedis.equalsIgnoreCase(dto.getVerificationCode())) {
            return ApiResult.failed("验证码错误");
        }
        // 校验通过后删除，避免重复使用
        redisTemplate.delete(verifyKey);
        // 2. 验证用户
        ModUsers user = iModUsersService.login(dto);
        if (user == null) {
            return ApiResult.failed("用户名或密码错误");
        }
        // 3. 生成 Token（需确保 JJWT 依赖已添加）
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",user.getUserId());
        claims.put("name",user.getUsername());
        String token = JwtUtils.generateToken(claims);

        // 4. 存入 Redis 并设置过期时间
        redisTemplate.opsForValue().set(
                RedisConstants.USER_TOKEN_KEY_PREFIX + user.getUsername() + ":token",
                token,
                RedisConstants.TOKEN_TTL,
                TimeUnit.MINUTES
        );


        // 5. 构建登录响应VO
        LoginResponseVO responseVO = LoginResponseVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .token(token)
                .permission(user.getPermission())
                .phone(user.getPhone())
                .build();

        return ApiResult.success(responseVO);
    }
}
