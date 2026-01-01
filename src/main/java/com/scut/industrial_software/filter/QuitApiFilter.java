package com.scut.industrial_software.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class QuitApiFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            String requestURI = request.getRequestURI();
            String contextPath = request.getContextPath(); // 可能为空字符串

            // 构造要检测的前缀，例如 "/api" 或 "/my-app/api"
            String apiPrefix = (contextPath == null ? "" : contextPath) + "/api";

            // 检查并去除前缀
            if (requestURI.startsWith(apiPrefix)) {
                String newRequestURI = requestURI.substring(apiPrefix.length());

                // 如果去掉后为空（例如请求是 "/api"），则默认为 "/"
                if (newRequestURI.isEmpty()) {
                    newRequestURI = "/";
                }

                // 如果 contextPath 不为空，需要补回 contextPath，保证 requestURI 格式正确
                if (contextPath != null && !contextPath.isEmpty()) {
                    newRequestURI = contextPath + newRequestURI;
                }

                log.info("原始 URI: {} -> 修改后 URI: {}", requestURI, newRequestURI);

                // 使用自定义 Wrapper 替换原 request
                request = new RequestWrapper(request, newRequestURI);
            }
        } catch (Exception e) {
            log.error("TokenFilter 处理 URI 前缀时发生异常", e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 自定义 RequestWrapper，用于修改 URI、URL 和 ServletPath
     */
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
        public String getServletPath() {
            // 对于 Spring MVC，ServletPath 通常是 URI 去掉 ContextPath 的部分
            String contextPath = getContextPath();
            if (contextPath != null && !contextPath.isEmpty() && newRequestURI.startsWith(contextPath)) {
                return newRequestURI.substring(contextPath.length());
            }
            return newRequestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            String scheme = getScheme();
            int port = getServerPort();

            url.append(scheme).append("://").append(getServerName());

            // 只有非默认端口才显示端口号 (http=80, https=443)
            boolean isDefaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
            if (!isDefaultPort) {
                url.append(":").append(port);
            }

            url.append(newRequestURI);
            return url;
        }
    }
}
