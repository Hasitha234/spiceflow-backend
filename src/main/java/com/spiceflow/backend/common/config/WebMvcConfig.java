package com.spiceflow.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final EndpointLoggingInterceptor loggingInterceptor;

    public WebMvcConfig(EndpointLoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply this logging exclusively to our API paths
        registry.addInterceptor(loggingInterceptor).addPathPatterns("/api/**");
    }
}
