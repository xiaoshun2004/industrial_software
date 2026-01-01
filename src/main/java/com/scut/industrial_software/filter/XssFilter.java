package com.scut.industrial_software.filter;

import com.scut.industrial_software.utils.XssFilterCoreUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XSS 过滤器，防止跨站脚本攻击
 */
@Slf4j
@Component
public class XssFilter implements Filter {
    /*
     * 是否过滤富文本内容，可以统一放在配置区
     */
    private static boolean IS_INCLUDE_RICH_TEXT = false;

    /*
     * 预设白名单列表，对此做直接放行处理
     */
    public List<String> excludes = new ArrayList<>();

    public static boolean isIsIncludeRichText() {
        return IS_INCLUDE_RICH_TEXT;
    }

    /**
     * 初始化拦截器配置
     * @param filterConfig 传入过滤器配置
     * @throws ServletException 初始化Servlet容器异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if(log.isDebugEnabled()){
            log.debug("----------------- xss filter init -----------------");
        }
        //获取其初始化时预设置的深度过滤开关，根据其预设的true、false进行确定其是否开启深度拦截
        String isIncludeRichText = filterConfig.getInitParameter("isIncludeRichText");
        if(StringUtils.isNotBlank(isIncludeRichText)){
            IS_INCLUDE_RICH_TEXT = BooleanUtils.toBoolean(isIncludeRichText);
        }
        //获取其初始化时预设置的白名单字符串，根据，符号进行截取存储。
        String temp = filterConfig.getInitParameter("excludes");
        if (temp != null) {
            String[] url = temp.split(",");
            excludes.addAll(Arrays.asList(url));
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(log.isDebugEnabled()){
            log.debug("-------------------- get into xss filter --------------------");
        }
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        // 1. 进行白名单过滤，如果符合白名单直接放行
        if (handleExcludeUrl(req,resp)){
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 2. 不符合白名单，进行XSS过滤处理
        XssRequestWrapper xssRequestWrapper = new XssRequestWrapper(req, IS_INCLUDE_RICH_TEXT);
        filterChain.doFilter(xssRequestWrapper, servletResponse);
    }

    /*
     * 处理白名单URL，如果符合白名单则返回true，否则返回false
     */
    private boolean handleExcludeUrl(HttpServletRequest req, HttpServletResponse resp) {
        // 1. 白名单为空时，直接返回false
        if (excludes == null || excludes.isEmpty()) {
            return false;
        }
        // 2. 获取请求的URL路径
        String url = req.getServletPath();
        // 3. 遍历白名单，进行匹配
        for (String pattern : excludes) {
            // 3.1 将白名单中的模式转换为正则表达式
            Pattern p = Pattern.compile("^" + pattern);
            Matcher m = p.matcher(url);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }

    // 自定义 XSS 过滤的 RequestWrapper（仓库），用于处理所有请求相关的字符串
    private static class XssRequestWrapper extends HttpServletRequestWrapper {
        /*
         * 需要进行过滤的请求
         */
        @Getter
        HttpServletRequest orgRequest = null;
        /*
         * 是否启用过滤
         */
        boolean isIncludeRichText;

        /**
         * 深度构造函数，传入需要过滤的请求和是否包含富文本的选项
         * @param request 需要过滤的请求
         * @param isIncludeRichText 是否包含富文本
         */
        public XssRequestWrapper(HttpServletRequest request, boolean isIncludeRichText) {
            super(request);
            orgRequest = request;
            this.isIncludeRichText = isIncludeRichText;
        }

        /**
         * 覆盖getParameter方法，将参数名和参数值都做xss过滤。<br/>
         * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取<br/>
         * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖
         */
        @Override
        public String getParameter(String name) {
            boolean flag = ("content".equals(name) || name.endsWith("WithHtml"));
            if( flag && !isIncludeRichText){
                return super.getParameter(name);
            }
            // 过滤参数名
            name = XssFilterCoreUtil.clean(name);
            // 过滤参数值
            String value = super.getParameter(name);
            if (StringUtils.isNotBlank(value)) {
                value = XssFilterCoreUtil.clean(value);
            }
            return value;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] arr = super.getParameterValues(name);
            if(arr != null){
                for (int i=0; i<arr.length; i++) {
                    arr[i] = XssFilterCoreUtil.clean(arr[i]);
                }
            }
            return arr;
        }

        /**
         * 覆盖getHeader方法，将参数名和参数值都做xss过滤。<br/>
         * 如果需要获得原始的值，则通过super.getHeaders(name)来获取<br/>
         * getHeaderNames 也可能需要覆盖
         */
        @Override
        public String getHeader(String name) {
            name = XssFilterCoreUtil.clean(name);
            String value = super.getHeader(name);
            if (StringUtils.isNotBlank(value)) {
                value = XssFilterCoreUtil.clean(value);
            }
            return value;
        }

        /**
         * 获取最原始的request的静态方法
         *
         * @return 返回最原始的request
         */
        public static HttpServletRequest getOrgRequest(HttpServletRequest req) {
            if (req instanceof XssRequestWrapper) {
                return ((XssRequestWrapper) req).getOrgRequest();
            }
            return req;
        }

    }
}
