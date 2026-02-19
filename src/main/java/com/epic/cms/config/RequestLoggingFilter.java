package com.epic.cms.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Component
@Slf4j
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        // Log incoming request
        log.info("========== INCOMING REQUEST ==========");
        log.info("Method: {}", httpRequest.getMethod());
        log.info("URL: {}", httpRequest.getRequestURL());
        log.info("URI: {}", httpRequest.getRequestURI());
        log.info("Query String: {}", httpRequest.getQueryString());
        log.info("Remote Address: {}", httpRequest.getRemoteAddr());
        
        // Log headers
        log.info("Headers:");
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.info("  {}: {}", headerName, httpRequest.getHeader(headerName));
        }
        
        // Log authentication info
        log.info("Auth Type: {}", httpRequest.getAuthType());
        log.info("Remote User: {}", httpRequest.getRemoteUser());
        log.info("User Principal: {}", httpRequest.getUserPrincipal());

        try {
            // Continue with the request
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log response
            log.info("========== RESPONSE ==========");
            log.info("Status: {}", httpResponse.getStatus());
            log.info("Duration: {} ms", duration);
            log.info("===================================");
        }
    }
}
