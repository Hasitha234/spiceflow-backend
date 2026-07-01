package com.spiceflow.backend.auth.service;

public interface TokenBlacklistService {

    /**
     * Adds a token to the blacklist until it naturally expires.
     * @param token the JWT token to blacklist
     * @param timeToLiveMillis the remaining time until the token expires
     */
    void blacklistToken(String token, long timeToLiveMillis);

    /**
     * Checks if a token is blacklisted.
     * @param token the JWT token to check
     * @return true if the token is blacklisted, false otherwise
     */
    boolean isBlacklisted(String token);
}
