package com.spiceflow.backend.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitProperties rateLimitProperties;
    private final Cache<String, Bucket> buckets;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitProperties rateLimitProperties) {
        this.rateLimitProperties = rateLimitProperties;
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(100_000)
                .build();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // Skip infrastructure paths
        if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveBucketKey(request);
        Bucket bucket = buckets.get(key, this::createNewBucket);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("Rate limit exceeded Key={} Endpoint={} Remaining=0", key, path);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            org.springframework.http.ProblemDetail problem = org.springframework.http.ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Please try again later.");
            problem.setTitle("Too Many Requests");
            problem.setInstance(java.net.URI.create(request.getRequestURI()));
            problem.setProperty("timestamp", OffsetDateTime.now(java.time.ZoneId.systemDefault()).toString());

            response.getWriter().write(objectMapper.writeValueAsString(problem));
        }
    }

    private String resolveBucketKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }
        return "ip:" + extractIpAddress(request);
    }

    private String extractIpAddress(HttpServletRequest request) {
        // Rely exclusively on the Servlet API's getRemoteAddr().
        // If we are behind a proxy, Spring's ForwardedHeaderFilter
        // (enabled via server.forward-headers-strategy=FRAMEWORK)
        // will safely extract the real IP. We do not trust X-Forwarded-For blindly here.
        return request.getRemoteAddr();
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit;
        if (key.startsWith("user:")) {
            RateLimitProperties.User userConfig = rateLimitProperties.getUser();
            limit = Bandwidth.builder()
                    .capacity(userConfig.getCapacity())
                    .refillIntervally(userConfig.getRefill(), Duration.ofSeconds(userConfig.getDuration()))
                    .build();
        } else {
            RateLimitProperties.Ip ipConfig = rateLimitProperties.getIp();
            limit = Bandwidth.builder()
                    .capacity(ipConfig.getCapacity())
                    .refillIntervally(ipConfig.getRefill(), Duration.ofSeconds(ipConfig.getDuration()))
                    .build();
        }
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
