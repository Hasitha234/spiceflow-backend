package com.spiceflow.backend.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class EndpointLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Record exactly when the request started
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        
        // Log at DEBUG level as soon as a request hits the server
        log.debug("Incoming Request: {} {}", request.getMethod(), request.getRequestURI());
        return true; // continue execution
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Calculate the exact execution time
        long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long duration = System.currentTimeMillis() - startTime;
        
        // Grab the user who made the request (if authenticated)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser"))
                ? auth.getName() : "Anonymous";

        // Determine if it was an error or success to adjust log format slightly if needed,
        // but log.info gives us a beautiful audit trail of all traffic.
        log.info("Endpoint: {} {} | Status: {} | User: {} | Time: {}ms",
                request.getMethod(), request.getRequestURI(), response.getStatus(), user, duration);
    }
}
