package com.spiceflow.backend.auth.filter;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.UserRepository;
import com.spiceflow.backend.auth.service.TokenBlacklistService;
import com.spiceflow.backend.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.spiceflow.backend.admin.repository.PlatformAdminRepository platformAdminRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldAuthenticateUser() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String email = "test@example.com";
        when(tokenBlacklistService.isBlacklisted("valid.token.here")).thenReturn(false);
        when(jwtUtil.extractEmail("valid.token.here")).thenReturn(email);
        when(jwtUtil.extractUserType("valid.token.here")).thenReturn("SYSTEM_ADMIN");
        when(jwtUtil.isTokenValid("valid.token.here")).thenReturn(true);

        User user = new User();
        user.setEmail(email);
        user.setUserType("SYSTEM_ADMIN");
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        user.setTenant(tenant);

        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> tenantMap = new HashMap<>();
        tenantMap.put("id", 1L);
        tenantMap.put("status", "ACTIVE");
        map.put("assignedTenants", List.of(tenantMap));
        Claims claims = new DefaultClaims(map);
        when(jwtUtil.parseClaims("valid.token.here")).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithExpiredTenant_ShouldReturnForbidden() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expired.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String email = "test@example.com";
        when(tokenBlacklistService.isBlacklisted("expired.token.here")).thenReturn(false);
        when(jwtUtil.extractEmail("expired.token.here")).thenReturn(email);
        when(jwtUtil.extractUserType("expired.token.here")).thenReturn("DATA_ENTRY_OPERATOR");
        when(jwtUtil.isTokenValid("expired.token.here")).thenReturn(true);

        User user = new User();
        user.setEmail(email);
        user.setUserType("DATA_ENTRY_OPERATOR");
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        user.setTenant(tenant);

        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> tenantMap = new HashMap<>();
        tenantMap.put("id", 1L);
        tenantMap.put("status", "EXPIRED");
        map.put("assignedTenants", List.of(tenantMap));
        Claims claims = new DefaultClaims(map);
        when(jwtUtil.parseClaims("expired.token.here")).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithDisabledTenant_ShouldReturnForbidden() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer disabled.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String email = "test@example.com";
        when(tokenBlacklistService.isBlacklisted("disabled.token.here")).thenReturn(false);
        when(jwtUtil.extractEmail("disabled.token.here")).thenReturn(email);
        when(jwtUtil.extractUserType("disabled.token.here")).thenReturn("DATA_ENTRY_OPERATOR");
        when(jwtUtil.isTokenValid("disabled.token.here")).thenReturn(true);

        User user = new User();
        user.setEmail(email);
        user.setUserType("DATA_ENTRY_OPERATOR");
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        user.setTenant(tenant);

        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> tenantMap = new HashMap<>();
        tenantMap.put("id", 1L);
        tenantMap.put("status", "DISABLED");
        map.put("assignedTenants", List.of(tenantMap));
        Claims claims = new DefaultClaims(map);
        when(jwtUtil.parseClaims("disabled.token.here")).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithoutAssignedTenants_ShouldProceed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String email = "test@example.com";
        when(tokenBlacklistService.isBlacklisted("valid.token.here")).thenReturn(false);
        when(jwtUtil.extractEmail("valid.token.here")).thenReturn(email);
        when(jwtUtil.extractUserType("valid.token.here")).thenReturn("SYSTEM_ADMIN");
        when(jwtUtil.isTokenValid("valid.token.here")).thenReturn(true);

        User user = new User();
        user.setEmail(email);
        user.setUserType("SYSTEM_ADMIN");
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        user.setTenant(tenant);

        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));

        Map<String, Object> map = new HashMap<>();
        // No assignedTenants claim
        Claims claims = new DefaultClaims(map);
        when(jwtUtil.parseClaims("valid.token.here")).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithMissingToken_ShouldProceed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithBlacklistedToken_ShouldProceed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer blacklisted.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        when(tokenBlacklistService.isBlacklisted("blacklisted.token.here")).thenReturn(true);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldProceed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        when(tokenBlacklistService.isBlacklisted("invalid.token.here")).thenReturn(false);
        when(jwtUtil.isTokenValid("invalid.token.here")).thenReturn(false);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithAlreadyAuthenticated_ShouldProceed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        when(tokenBlacklistService.isBlacklisted("valid.token.here")).thenReturn(false);
        when(jwtUtil.extractEmail("valid.token.here")).thenReturn("test@example.com");
        when(jwtUtil.extractUserType("valid.token.here")).thenReturn("SYSTEM_ADMIN");
        when(jwtUtil.isTokenValid("valid.token.here")).thenReturn(true);
        
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "pass"));
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithPlatformAdmin_ShouldAuthenticate() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        String email = "admin@platform.com";
        when(tokenBlacklistService.isBlacklisted("valid.token.here")).thenReturn(false);
        when(jwtUtil.extractEmail("valid.token.here")).thenReturn(email);
        when(jwtUtil.extractUserType("valid.token.here")).thenReturn("PLATFORM_ADMIN");
        when(jwtUtil.isTokenValid("valid.token.here")).thenReturn(true);
        
        com.spiceflow.backend.admin.entity.PlatformAdmin admin = new com.spiceflow.backend.admin.entity.PlatformAdmin();
        admin.setEmail(email);
        when(platformAdminRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(admin));
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithUserDisabled_ShouldProceedWithoutAuthenticating() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        String email = "test@example.com";
        when(tokenBlacklistService.isBlacklisted("valid.token.here")).thenReturn(false);
        when(jwtUtil.extractEmail("valid.token.here")).thenReturn(email);
        when(jwtUtil.extractUserType("valid.token.here")).thenReturn("DATA_ENTRY_OPERATOR");
        when(jwtUtil.isTokenValid("valid.token.here")).thenReturn(true);
        
        User user = new User();
        user.setEmail(email);
        user.setDeletedAt(java.time.OffsetDateTime.now()); // Disabled
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithTenantOwner_ValidHeader_ShouldUseHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer owner.token.here");
        request.addHeader("X-Tenant-ID", "2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        String email = "owner@example.com";
        when(tokenBlacklistService.isBlacklisted("owner.token.here")).thenReturn(false);
        when(jwtUtil.extractEmail("owner.token.here")).thenReturn(email);
        when(jwtUtil.extractUserType("owner.token.here")).thenReturn("TENANT_OWNER");
        when(jwtUtil.isTokenValid("owner.token.here")).thenReturn(true);
        
        User user = new User();
        user.setEmail(email);
        user.setUserType("TENANT_OWNER");
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        user.setTenant(tenant); // activeTenantId is initially 1
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));
        
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> tenantMap = new HashMap<>();
        tenantMap.put("id", 2L);
        tenantMap.put("status", "ACTIVE");
        map.put("assignedTenants", List.of(tenantMap));
        Claims claims = new DefaultClaims(map);
        when(jwtUtil.parseClaims("owner.token.here")).thenReturn(claims);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithTenantOwner_InvalidHeader_ShouldIgnoreHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer owner.token.here");
        request.addHeader("X-Tenant-ID", "not-a-number");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        String email = "owner@example.com";
        when(tokenBlacklistService.isBlacklisted("owner.token.here")).thenReturn(false);
        when(jwtUtil.extractEmail("owner.token.here")).thenReturn(email);
        when(jwtUtil.extractUserType("owner.token.here")).thenReturn("TENANT_OWNER");
        when(jwtUtil.isTokenValid("owner.token.here")).thenReturn(true);
        
        User user = new User();
        user.setEmail(email);
        user.setUserType("TENANT_OWNER");
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        user.setTenant(tenant);
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));
        
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> tenantMap = new HashMap<>();
        tenantMap.put("id", 1L);
        tenantMap.put("status", "ACTIVE");
        map.put("assignedTenants", List.of(tenantMap));
        Claims claims = new DefaultClaims(map);
        when(jwtUtil.parseClaims("owner.token.here")).thenReturn(claims);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
