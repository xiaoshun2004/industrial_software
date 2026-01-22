package com.scut.industrial_software.filter;

import cn.hutool.core.util.StrUtil;
import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.service.impl.TokenBlacklistService;
import com.scut.industrial_software.utils.JwtUtils;
import com.scut.industrial_software.utils.LuaScriptConstants;
import com.scut.industrial_software.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

import static com.scut.industrial_software.model.constant.RedisConstants.TOKEN_TTL;
import static com.scut.industrial_software.model.constant.RedisConstants.USER_TOKEN_KEY_PREFIX;

// 该刷新拦截器用于刷新用户的登录状态，属于第一层拦截器
@Slf4j
@Component
public class RefreshInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private final TokenBlacklistService tokenBlacklistService;

    public RefreshInterceptor(StringRedisTemplate stringRedisTemplate, TokenBlacklistService tokenBlacklistService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头中获取token
        String token = request.getHeader("Authorization");
        log.info("RefreshInterceptor: 获取到的token为 {}", token);

        // 2. 判断token是否为空
        if (StrUtil.isBlank(token)){
            // 2.1 token为空，第一层拦截器直接放行，交给后续拦截器处理
            return true;
        }

        // 3. 检查token是否存在于黑名单中
        if (tokenBlacklistService.isTokenBlacklisted(token)){
            log.info("RefreshInterceptor: token在黑名单中，拒绝访问");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // Token 在黑名单中，返回 403
            response.getWriter().write("Token in blacklist");
            return false;
        }

        // 4. 如果token存在且不在黑名单中，检验令牌的有效性并刷新登录状态
        try{
            log.info("RefreshInterceptor: 获取到的 Token: {}", token);

            // 4.1 解析token获取Claims
            Claims claims = JwtUtils.parseToken(token);

            // 4.2 从Claims提取用户信息并存储到UserHolder
            Integer userId = claims.get("id", Integer.class);
            String name = claims.get("name", String.class);

            // 4.3 创建UserDTO对象并设置属性
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userId);
            userDTO.setName(name);

            // 4.4 使用原子化Lua脚本的方式验证token有效性并刷新过期时间
            boolean isTokenValid = validateAndRefreshToken(name, token);
            if (!isTokenValid) {
                log.info("Token 验证失败或已过期，响应401");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token invalid or expired");
                return false;
            }

            log.info("当前用户id，name：{},{}",userId,name);
            // 设置其他需要的字段...

            // 4.5 将用户信息保存到ThreadLocal
            UserHolder.saveUser(userDTO);

        } catch (Exception e) {
            log.info("令牌非法, 响应401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 5. 放行
        return true;
    }

    /**
     * 原子化验证token并刷新过期时间
     * 使用Lua脚本确保操作的原子性，避免竞态条件
     */
    private boolean validateAndRefreshToken(String username, String token) {
        String tokenKey = USER_TOKEN_KEY_PREFIX + username + ":token";

        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(LuaScriptConstants.VALIDATE_AND_REFRESH_TOKEN_SCRIPT);
            script.setResultType(Long.class);

            Long result = stringRedisTemplate.execute(script,
                    Arrays.asList(tokenKey),
                    token,
                    String.valueOf(TOKEN_TTL * 60));

            boolean isValid = result != null && result == 1;
            if (isValid) {
                log.debug("Token验证成功并刷新过期时间: {}", username);
            } else {
                log.warn("Token验证失败: {} - token可能已过期或被其他设备替换", username);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Token验证过程中发生异常: {}", username, e);
            return false;
        }
    }

}
