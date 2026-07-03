package com.spiceflow.backend.common.config;

import com.spiceflow.backend.common.context.TenantContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * Global Async Configuration ensuring threadsafe propagation of Tenant and Security contexts.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncThread-");
        
        // AbortPolicy (default) throws RejectedExecutionException instead of blocking the request thread (CallerRunsPolicy)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        
        // Ensure ThreadLocal contexts (Security + Tenant) propagate to async threads
        executor.setTaskDecorator(new ContextCopyingDecorator());
        
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, obj) -> {
            log.error("Exception message - {}", throwable.getMessage());
            log.error("Method name - {}", method.getName());
            for (Object param : obj) {
                log.error("Parameter value - {}", param);
            }
        };
    }

    /**
     * Copies the current thread's context into the async execution thread.
     */
    static class ContextCopyingDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            Long tenantId = TenantContext.getTenantId();
            
            return () -> {
                try {
                    SecurityContextHolder.setContext(securityContext);
                    TenantContext.setTenantId(tenantId);
                    runnable.run();
                } finally {
                    SecurityContextHolder.clearContext();
                    TenantContext.clear();
                }
            };
        }
    }
}
