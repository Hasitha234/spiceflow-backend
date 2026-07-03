package com.spiceflow.backend.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private RateLimitProperties properties;
    private FilterChain filterChain;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.getIp().setCapacity(3);
        properties.getIp().setRefill(3);
        properties.getIp().setDuration(60);
        
        rateLimitFilter = new RateLimitFilter(properties);
        filterChain = mock(FilterChain.class);
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldAllowRequestsUnderLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 3 requests allowed
        for (int i = 0; i < 3; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        verify(filterChain, times(3)).doFilter(request, response);
    }

    @Test
    void shouldBlockRequestsOverLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Exhaust the bucket
        for (int i = 0; i < 3; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        // 4th request should fail
        response = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader("X-RateLimit-Retry-After-Seconds")).isNotNull();
        
        String jsonBody = response.getContentAsString();
        assertThat(jsonBody.toLowerCase()).contains("too many requests");
        
        verify(filterChain, times(3)).doFilter(any(), any());
    }

    @Test
    void shouldIgnoreXForwardedForHeader() throws ServletException, IOException {
        // Attack: attempt to bypass rate limit by sending different X-Forwarded-For headers
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.100");
        request.setRemoteAddr("127.0.0.1"); // Real IP
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Exhaust the limit for 127.0.0.1
        for (int i = 0; i < 3; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        // 4th request from a "different" X-Forwarded-For IP, but same real IP
        MockHttpServletRequest spoofedRequest = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        spoofedRequest.addHeader("X-Forwarded-For", "20.0.0.2");
        spoofedRequest.setRemoteAddr("127.0.0.1"); // Same real IP
        MockHttpServletResponse spoofedResponse = new MockHttpServletResponse();
        
        rateLimitFilter.doFilterInternal(spoofedRequest, spoofedResponse, filterChain);
        
        // It should still fail, proving X-Forwarded-For is ignored
        assertThat(spoofedResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void shouldSkipInfrastructureEndpoints() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Send 10 requests, all should pass because the filter ignores infrastructure endpoints
        for (int i = 0; i < 10; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(10)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
