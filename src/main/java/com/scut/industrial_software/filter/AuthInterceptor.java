package com.scut.industrial_software.filter;

import com.scut.industrial_software.model.dto.UserDTO;
import com.scut.industrial_software.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 经过刷新拦截器后进入登录拦截器，第二层拦截器
@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 判断threadLocal中是否有用户
        UserDTO user = UserHolder.getUser();
        // 短暂的小测试，获取请求的路径
        String requestURI = request.getRequestURI();
        log.info("Intercepted request to: {}", requestURI);

        if (user == null) {
            // 2. 没有，拦截，返回状态码401
            log.info("Token 验证失败或已过期，响应401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalid or expired");
            return false;
        }
        // 3. 有，放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户信息
        UserHolder.removeUser();
    }

}
