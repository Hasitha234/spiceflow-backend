package com.spiceflow.backend.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.spiceflow.backend.security.config.RateLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final RateLimitProperties rateLimitProperties;
    private final Cache<String, Integer> attemptsCache;

    public LoginAttemptService(RateLimitProperties rateLimitProperties) {
        this.rateLimitProperties = rateLimitProperties;
        this.attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(rateLimitProperties.getAccount().getLockoutDurationMinutes(), TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    public void loginSucceeded(String username) {
        if (username != null) {
            attemptsCache.invalidate(username);
        }
    }

    public void loginFailed(String username) {
        if (username != null) {
            int attempts = attemptsCache.get(username, key -> 0);
            attempts++;
            attemptsCache.put(username, attempts);
            
            if (attempts >= rateLimitProperties.getAccount().getMaxFailedAttempts()) {
                log.warn("Account lockout threshold reached in cache for user: {}", username);
            }
        }
    }

    public boolean isBlocked(String username) {
        int attempts = attemptsCache.get(username, key -> 0);
        return attempts >= rateLimitProperties.getAccount().getMaxFailedAttempts();
    }
}
