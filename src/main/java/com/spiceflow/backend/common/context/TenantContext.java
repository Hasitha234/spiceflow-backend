package com.spiceflow.backend.common.context;

public class TenantContext {
    private static final ThreadLocal<Long> currentTenant = new InheritableThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        currentTenant.set(tenantId);
    }

    public static Long getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
