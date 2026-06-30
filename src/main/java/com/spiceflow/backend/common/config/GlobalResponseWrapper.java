package com.spiceflow.backend.common.config;

import com.spiceflow.backend.common.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.spiceflow.backend")
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Do not intercept if it's already an ApiResponse (e.g., from GlobalExceptionHandler)
        // or if it's Swagger/OpenAPI docs
        if (returnType.getParameterType().isAssignableFrom(ApiResponse.class)) {
            return false;
        }
        String className = returnType.getDeclaringClass().getName();
        if (className.contains("springdoc") || className.contains("swagger")) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        int status = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse) {
            status = ((ServletServerHttpResponse) response).getServletResponse().getStatus();
        }

        // Do not wrap if it's a 204 No Content or body is null
        if (status == HttpStatus.NO_CONTENT.value() || body == null) {
            return body;
        }

        // Exclude files/binary
        if (body instanceof org.springframework.core.io.Resource || body instanceof byte[]) {
            return body;
        }

        // If the body is already an ApiResponse, return it as is
        if (body instanceof ApiResponse) {
            return body;
        }

        // Wrap the payload
        return ApiResponse.success(body);
    }
}
