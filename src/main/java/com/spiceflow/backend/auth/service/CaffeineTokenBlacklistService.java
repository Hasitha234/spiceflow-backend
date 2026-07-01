package com.spiceflow.backend.auth.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CaffeineTokenBlacklistService implements TokenBlacklistService {

    // Cache that expires entries based on a dynamic TTL (the remaining time of the JWT)
    private final Cache<String, Long> blacklistCache;

    public CaffeineTokenBlacklistService() {
        this.blacklistCache = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, Long>() {
                    @Override
                    public long expireAfterCreate(String key, Long timeToLiveMillis, long currentTime) {
                        return TimeUnit.MILLISECONDS.toNanos(timeToLiveMillis);
                    }

                    @Override
                    public long expireAfterUpdate(String key, Long value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(String key, Long value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    @Override
    public void blacklistToken(String token, long timeToLiveMillis) {
        if (timeToLiveMillis > 0) {
            blacklistCache.put(token, timeToLiveMillis);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklistCache.getIfPresent(token) != null;
    }
}
