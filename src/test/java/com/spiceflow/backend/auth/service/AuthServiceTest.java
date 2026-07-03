package com.spiceflow.backend.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.admin.entity.PlatformAdmin;
import com.spiceflow.backend.admin.repository.PlatformAdminRepository;
import com.spiceflow.backend.auth.dto.request.ChangePasswordRequest;
import com.spiceflow.backend.auth.dto.request.LoginRequest;
import com.spiceflow.backend.auth.dto.request.TokenRefreshRequest;
import com.spiceflow.backend.auth.dto.response.LoginResponse;
import com.spiceflow.backend.auth.entity.RefreshToken;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.auth.repository.RefreshTokenRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import com.spiceflow.backend.auth.util.JwtUtil;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.InvalidCredentialsException;
import com.spiceflow.backend.security.service.LoginAttemptService;
import com.spiceflow.backend.auth.entity.Tenant;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PlatformAdminRepository platformAdminRepository;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @InjectMocks private AuthService authService;

    private User user;
    private PlatformAdmin admin;
    private Tenant tenant;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setStatus("ACTIVE");

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed_password");
        user.setTenant(tenant);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        user.setPasswordChangeRequired(false);

        admin = new PlatformAdmin();
        admin.setId(1L);
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("hashed_admin_password");

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setTokenHash("mocked_hash");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(1));
    }

    @Test
    void login_PlatformAdmin_Success() {
        LoginRequest request = new LoginRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "email", "admin@example.com");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "password", "password");
        when(platformAdminRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                .thenReturn(Optional.of(admin));
        when(loginAttemptService.isBlocked(admin.getEmail())).thenReturn(false);
        when(passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateAdminToken(admin)).thenReturn("access_token");
        when(jwtUtil.getAccessTokenExpiryMs()).thenReturn(3600000L);
        when(jwtUtil.getRefreshTokenExpiryMs()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertFalse(response.isPasswordChangeRequired());
        verify(loginAttemptService).loginSucceeded(admin.getEmail());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_PlatformAdmin_InvalidPassword() {
        LoginRequest request = new LoginRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "email", "admin@example.com");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "password", "wrong_password");
        when(platformAdminRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                .thenReturn(Optional.of(admin));
        when(loginAttemptService.isBlocked(admin.getEmail())).thenReturn(false);
        when(passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(loginAttemptService).loginFailed(admin.getEmail());
    }

    @Test
    void login_User_Success() {
        LoginRequest request = new LoginRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "email", "user@example.com");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "password", "password");
        when(platformAdminRepository.findByEmailAndDeletedAtIsNull(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())).thenReturn(Optional.of(user));
        when(loginAttemptService.isBlocked(user.getEmail())).thenReturn(false);
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateAccessToken(user)).thenReturn("access_token");
        when(jwtUtil.getAccessTokenExpiryMs()).thenReturn(3600000L);
        when(jwtUtil.getRefreshTokenExpiryMs()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertFalse(response.isPasswordChangeRequired());
        verify(loginAttemptService).loginSucceeded(user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void login_User_InactiveTenant_ThrowsAccessDenied() {
        LoginRequest request = new LoginRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "email", "user@example.com");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "password", "password");
        tenant.setStatus("SUSPENDED");
        when(platformAdminRepository.findByEmailAndDeletedAtIsNull(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())).thenReturn(Optional.of(user));
        assertThrows(AccessDeniedException.class, () -> authService.login(request));
    }

    @Test
    void login_User_LockedAccount_ThrowsInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "email", "user@example.com");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "password", "password");
        user.setLockedUntil(OffsetDateTime.now().plusMinutes(10));
        when(platformAdminRepository.findByEmailAndDeletedAtIsNull(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())).thenReturn(Optional.of(user));
        when(loginAttemptService.isBlocked(user.getEmail())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refresh_User_Success() {
        TokenRefreshRequest request = new TokenRefreshRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "refreshToken", "raw_refresh_token");
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateAccessToken(user)).thenReturn("new_access_token");
        when(jwtUtil.getAccessTokenExpiryMs()).thenReturn(3600000L);

        LoginResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("raw_refresh_token", response.getRefreshToken());
        assertFalse(response.isPasswordChangeRequired());
    }

    @Test
    void logout_Success() {
        String accessToken = "access_token";
        String rawRefreshToken = "raw_refresh_token";
        
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.isTokenValid(accessToken)).thenReturn(true);
        when(jwtUtil.getRemainingTimeInMillis(accessToken)).thenReturn(1000L);

        authService.logout(rawRefreshToken, accessToken);

        assertNotNull(refreshToken.getRevokedAt());
        verify(refreshTokenRepository).save(refreshToken);
        verify(tokenBlacklistService).blacklistToken(accessToken, 1000L);
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "currentPassword", "hashed_password");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "newPassword", "new_password");
        when(passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("new_hashed_password");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        authService.changePassword(com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(user.getId()).tenantId(user.getTenant() != null ? user.getTenant().getId() : null).email(user.getEmail()).build(), request);

        assertEquals("new_hashed_password", user.getPasswordHash());
        assertFalse(user.isPasswordChangeRequired());
        verify(userRepository).save(user);
        verify(refreshTokenRepository).revokeAllByUserId(eq(user.getId()), any(OffsetDateTime.class));
    }

    @Test
    void changePassword_InvalidCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "currentPassword", "wrong_password");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "newPassword", "new_password");
        when(passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())).thenReturn(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleViolationException.class, () -> authService.changePassword(com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(user.getId()).tenantId(user.getTenant() != null ? user.getTenant().getId() : null).email(user.getEmail()).build(), request));
    }
}






