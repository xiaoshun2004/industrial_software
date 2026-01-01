package com.scut.industrial_software.config;

import com.scut.industrial_software.filter.AuthInterceptor;
import com.scut.industrial_software.filter.RefreshInterceptor;
import com.scut.industrial_software.service.impl.TokenBlacklistService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TokenBlacklistService tokenBlacklistService;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        // RefreshInterceptor should be registered before AuthInterceptor
        registry.addInterceptor(new RefreshInterceptor(stringRedisTemplate, tokenBlacklistService))
                .order(0); // Set order to ensure it runs first
        // AuthInterceptor is used to verify user authentication
        registry.addInterceptor(new AuthInterceptor())
                .excludePathPatterns(
                        "/auth/verifyCode",
                        "/auth/jsonLogin",
                        "/api/modUsers/register",
                        "/api/auth/jsonLogin",
                        "/api/auth/verifyCode"
                )
                .order(1); // Set order to ensure it runs after RefreshInterceptor
    }

}
