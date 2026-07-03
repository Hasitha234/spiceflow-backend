package com.spiceflow.backend.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private final com.spiceflow.backend.common.cache.TenantAwareKeyGenerator tenantAwareKeyGenerator;

    public CacheConfig(com.spiceflow.backend.common.cache.TenantAwareKeyGenerator tenantAwareKeyGenerator) {
        this.tenantAwareKeyGenerator = tenantAwareKeyGenerator;
    }

    @Override
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats());
        return cacheManager;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return tenantAwareKeyGenerator;
    }

    @Bean("simpleKeyGenerator")
    public KeyGenerator simpleKeyGenerator() {
        return new org.springframework.cache.interceptor.SimpleKeyGenerator();
    }
}
