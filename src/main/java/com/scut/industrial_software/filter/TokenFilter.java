package com.scut.industrial_software.filter;

import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.service.impl.TokenBlacklistService;
import com.scut.industrial_software.utils.JwtUtils;
import com.scut.industrial_software.utils.LuaScriptConstants;
import com.scut.industrial_software.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.script.DefaultRedisScript;

import static com.scut.industrial_software.model.constant.RedisConstants.USER_TOKEN_KEY_PREFIX;
import static com.scut.industrial_software.model.constant.RedisConstants.TOKEN_TTL;


@Slf4j
@Component
@WebFilter(urlPatterns = "/*")
public class TokenFilter implements Filter {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            //1. 获取到请求路径
            String requestURI = request.getRequestURI(); // /employee/login

            log.info("当前请求 URI: {}", requestURI);

            // 2. 检查 URI 是否以 /api 开头，如果是，去掉 /api 前缀
            if (requestURI.startsWith("/api")) {
                String newRequestURI = requestURI.substring(4); // 去掉 /api 部分
                log.info("修改后的请求 URI: {}", newRequestURI);

                // 更新请求 URI
                request = new RequestWrapper(request, newRequestURI);
                requestURI = newRequestURI;
            }

            // 2.放行注册和登录请求，以及文件接口请求
            if (requestURI.equals("/auth/verifyCode") || 
                requestURI.equals("/modUsers/register") || 
                requestURI.equals("/auth/jsonLogin") || 
                requestURI.equals("/api/modUsers/register") || 
                requestURI.equals("/api/auth/jsonLogin") || 
                requestURI.equals("/api/auth/verifyCode") ||
                requestURI.startsWith("/dataManagement/") ||
                requestURI.startsWith("/taskMonitor/")){  // 添加文件接口白名单
                
                // 如果是文件接口，设置一个默认用户，方便测试
                if (requestURI.startsWith("/dataManagement/")) {
                    UserDTO defaultUser = new UserDTO();
                    defaultUser.setId(1L);  // 设置一个默认ID
                    defaultUser.setName("测试用户");  // 设置一个默认用户名
                    UserHolder.saveUser(defaultUser);
                }
                
                log.info("放行请求: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            //3. 获取请求头中的token
            String token = request.getHeader("Authorization");
            log.info("au:"+token);

            //4. 判断token是否存在, 如果不存在, 说明用户没有登录, 返回错误信息(响应401状态码)
            if (token == null || token.isEmpty()){
                log.info("令牌为空, 响应401");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            //检查token是否在黑名单当中
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.info("token在黑名单当中,拒绝访问");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // Token 在黑名单中，返回 403
                response.getWriter().write("Token in blacklist");
                return;
            }

            //5. 如果token存在, 校验令牌, 如果校验失败 -> 返回错误信息(响应401状态码)
            try {
                log.info("获取到的 Token: {}", token);

                // 解析token获取Claims
                Claims claims = JwtUtils.parseToken(token);

                // 从Claims提取用户信息并存储到UserHolder
                Long userId = claims.get("id", Long.class);
                String name = claims.get("name", String.class);

                // 创建UserDTO对象并设置属性
                UserDTO userDTO = new UserDTO();
                userDTO.setId(userId);
                userDTO.setName(name);

                // 使用原子化的方式验证token有效性并刷新过期时间
                boolean isTokenValid = validateAndRefreshToken(name, token);
                if (!isTokenValid) {
                    log.info("Token 验证失败或已过期，响应401");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token invalid or expired");
                    return;
                }


                log.info("当前用户id，name：{},{}",userId,name);
                // 设置其他需要的字段...

                // 将用户信息保存到ThreadLocal
                UserHolder.saveUser(userDTO);

            } catch (Exception e) {
                log.info("令牌非法, 响应401");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }



            //6. 校验通过, 放行
            log.info("令牌合法, 放行");
            filterChain.doFilter(request, response);

        } finally {
            // 7. 请求处理完成后，清除ThreadLocal中的数据，防止内存泄漏
            UserHolder.removeUser();
        }
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
            
            Long result = redisTemplate.execute(script, 
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

    // 自定义一个RequestWrapper，用于修改请求URI
    private static class RequestWrapper extends HttpServletRequestWrapper {
        private final String newRequestURI;

        public RequestWrapper(HttpServletRequest request, String newRequestURI) {
            super(request);
            this.newRequestURI = newRequestURI;
        }

        @Override
        public String getRequestURI() {
            return newRequestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            return new StringBuffer(getScheme() + "://" + getServerName() + getServerPort() + newRequestURI);
        }
    }
}