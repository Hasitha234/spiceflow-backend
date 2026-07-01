package com.spiceflow.backend.common.aspect;

import com.spiceflow.backend.auth.entity.User;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect to automatically enable the Hibernate "tenantFilter"
 * on every repository query made by an authenticated tenant User.
 * 
 * This provides a critical safety net against cross-tenant data leakage.
 */
@Aspect
@Component
public class TenantFilterAspect {

    private final EntityManager entityManager;

    public TenantFilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // Intercept all methods in all repositories inside our application
    @Before("execution(* com.spiceflow.backend..*Repository.*(..))")
    public void enableTenantFilter() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Only apply if the current principal is a tenant User
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            
            if (user.getTenantId() != null) {
                // Unwrap the underlying Hibernate Session and enable the filter
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("tenantId", user.getTenantId());
            }
        }
    }
}
