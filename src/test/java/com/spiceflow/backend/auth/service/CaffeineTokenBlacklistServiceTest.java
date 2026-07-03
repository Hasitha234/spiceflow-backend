package com.spiceflow.backend.auth.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CaffeineTokenBlacklistServiceTest {

    private CaffeineTokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new CaffeineTokenBlacklistService();
    }

    @Test
    void blacklistToken_AddsToCache() {
        String token = "my_jwt_token";
        
        assertFalse(tokenBlacklistService.isBlacklisted(token));
        
        tokenBlacklistService.blacklistToken(token, 10000L);
        
        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void blacklistToken_NegativeTTL_DoesNotAdd() {
        String token = "my_jwt_token";
        
        tokenBlacklistService.blacklistToken(token, -100L);
        
        assertFalse(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void isBlacklisted_ReturnsFalseForUnknownToken() {
        assertFalse(tokenBlacklistService.isBlacklisted("unknown_token"));
    }
}
