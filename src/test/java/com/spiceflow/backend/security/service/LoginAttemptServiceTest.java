package com.spiceflow.backend.security.service;

import com.spiceflow.backend.security.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    private RateLimitProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.getAccount().setMaxFailedAttempts(3);
        properties.getAccount().setLockoutDurationMinutes(15);
        loginAttemptService = new LoginAttemptService(properties);
    }

    @Test
    void shouldBlockUserAfterMaxFailedAttempts() {
        String username = "test@example.com";

        assertThat(loginAttemptService.isBlocked(username)).isFalse();

        loginAttemptService.loginFailed(username);
        loginAttemptService.loginFailed(username);
        assertThat(loginAttemptService.isBlocked(username)).isFalse();

        loginAttemptService.loginFailed(username);
        assertThat(loginAttemptService.isBlocked(username)).isTrue();
    }

    @Test
    void shouldResetAttemptsOnSuccessfulLogin() {
        String username = "test2@example.com";

        loginAttemptService.loginFailed(username);
        loginAttemptService.loginFailed(username);
        
        loginAttemptService.loginSucceeded(username);
        
        loginAttemptService.loginFailed(username);
        
        assertThat(loginAttemptService.isBlocked(username)).isFalse();
    }
}
