package com.scut.industrial_software.filter;

import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.utils.JwtUtils;
import com.scut.industrial_software.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.RequestWrapper;

import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/*")
public class TokenFilter implements Filter {
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
            }


            // 2.放行注册和登录请求
            if (requestURI.equals("/modUsers/register") || requestURI.equals("/auth/jsonLogin")||requestURI.equals("/api/modUsers/register") || requestURI.equals("/api/auth/jsonLogin")|| requestURI.equals("/api/auth/verifyCode")) {
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