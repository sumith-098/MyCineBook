package com.cinebook.notification.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Every endpoint in this service is service-to-service only (called by booking-service), so
 * there's no JWT/Spring Security stack here at all — just a plain filter checking the shared
 * internal secret. /actuator/health is left open for container health checks.
 */
@Configuration
public class InternalApiKeyFilter {

    @Value("${app.internal.api-key}")
    private String expectedKey;

    @Bean
    public FilterRegistrationBean<Filter> internalApiKeyFilterRegistration() {
        Filter filter = (ServletRequest request, ServletResponse response, FilterChain chain) -> {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            if (req.getRequestURI().startsWith("/actuator/health")) {
                chain.doFilter(request, response);
                return;
            }

            String provided = req.getHeader("X-Internal-Api-Key");
            if (provided == null || !provided.equals(expectedKey)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"success\":false,\"message\":\"Missing or invalid X-Internal-Api-Key\"}");
                return;
            }
            chain.doFilter(request, response);
        };

        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/notifications/*");
        registration.setOrder(1);
        return registration;
    }
}
