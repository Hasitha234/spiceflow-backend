package com.spiceflow.backend.common.cache;

import com.spiceflow.backend.common.context.TenantContext;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Generates cache keys prefixed with the current Tenant ID.
 * This guarantees that tenant A's cached data can never be accidentally served to tenant B.
 * If there is no tenant in context (e.g. platform admin, background job), it uses a "GLOBAL" sentinel.
 */
@Component("tenantAwareKeyGenerator")
public class TenantAwareKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        Long tenantId = TenantContext.getTenantId();
        String prefix = (tenantId != null) ? tenantId.toString() : "GLOBAL";
        
        return prefix + ":" + SimpleKeyGenerator.generateKey(params);
    }
}
