package com.scut.industrial_software.config;

import com.scut.industrial_software.filter.QuitApiFilter;
import com.scut.industrial_software.filter.XssFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private final Logger log = LoggerFactory.getLogger(FilterConfig.class);

    /**
     * 配置XSS过滤器
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistrationBean() {
        log.info("-----------------初始化XSS过滤器-----------------");
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");      // 拦截所有请求
        registration.setOrder(0);
        registration.setName("XssFilter");
        return registration;
    }

    /**
     * 配置初始全局api去除拦截器
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<QuitApiFilter> apifilterRegistrationBean() {
        log.info("-----------------初始化全局API去除过滤器-----------------");
        // 1. 启动拦截器
        FilterRegistrationBean<QuitApiFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new QuitApiFilter());
        registration.addUrlPatterns("/*");      // 拦截所有请求
        registration.setOrder(1);
        registration.setName("MoveApiFilter");
        return registration;
    }
}
