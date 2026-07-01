package com.spiceflow.backend.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {
    private Ip ip = new Ip();
    private Account account = new Account();

    private User user = new User();

    @Data
    public static class Ip {
        private int capacity = 30;
        private int refill = 30;
        private int duration = 60;
    }

    @Data
    public static class User {
        private int capacity = 100;
        private int refill = 100;
        private int duration = 60;
    }

    @Data
    public static class Account {
        private int maxFailedAttempts = 5;
        private int lockoutDurationMinutes = 15;
    }
}
