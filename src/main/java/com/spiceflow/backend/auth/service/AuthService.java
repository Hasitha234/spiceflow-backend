package com.spiceflow.backend.auth.service;

import com.spiceflow.backend.admin.entity.PlatformAdmin;
import com.spiceflow.backend.admin.repository.PlatformAdminRepository;
import com.spiceflow.backend.auth.dto.request.ChangePasswordRequest;
import com.spiceflow.backend.auth.dto.request.LoginRequest;
import com.spiceflow.backend.auth.dto.request.TokenRefreshRequest;
import com.spiceflow.backend.auth.dto.response.LoginResponse;
import com.spiceflow.backend.auth.entity.RefreshToken;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.RefreshTokenRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import com.spiceflow.backend.auth.util.JwtUtil;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.InvalidCredentialsException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.spiceflow.backend.admin.entity.PlatformAdmin;
import com.spiceflow.backend.admin.repository.PlatformAdminRepository;
import com.spiceflow.backend.security.service.LoginAttemptService;
import java.util.Optional;


import org.springframework.transaction.annotation.Transactional;

/** Handles all authentication operations: login, refresh, logout, change password. */
@Service
@Transactional(readOnly = true)
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final int LOCKOUT_MINUTES = 15;

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;
  private final PlatformAdminRepository platformAdminRepository;
  private final LoginAttemptService loginAttemptService;
  private final TokenBlacklistService tokenBlacklistService;


  public AuthService(
    UserRepository userRepository,
    RefreshTokenRepository refreshTokenRepository,
    JwtUtil jwtUtil,
    PasswordEncoder passwordEncoder,
    PlatformAdminRepository platformAdminRepository,
    LoginAttemptService loginAttemptService,
    TokenBlacklistService tokenBlacklistService) {
  this.userRepository = userRepository;
  this.refreshTokenRepository = refreshTokenRepository;
  this.jwtUtil = jwtUtil;
  this.passwordEncoder = passwordEncoder;
  this.platformAdminRepository = platformAdminRepository;
  this.loginAttemptService = loginAttemptService;
  this.tokenBlacklistService = tokenBlacklistService;
}


  /**
   * Authenticates a user by email and password.
   * Enforces account lockout after 5 failed attempts.
   */
  @Transactional(rollbackFor = Exception.class)
public LoginResponse login(LoginRequest request) {
  // Check platform_admins table first
  Optional<PlatformAdmin> adminOpt =
      platformAdminRepository.findByEmailAndDeletedAtIsNull(request.getEmail());

  if (adminOpt.isPresent()) {
    PlatformAdmin admin = adminOpt.get();
    
    if (loginAttemptService.isBlocked(admin.getEmail())) {
      throw new InvalidCredentialsException("Account is temporarily locked due to too many failed attempts.");
    }
    
    if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
      loginAttemptService.loginFailed(admin.getEmail());
      throw new InvalidCredentialsException("Invalid email or password");
    }
    
    loginAttemptService.loginSucceeded(admin.getEmail());
    String accessToken = jwtUtil.generateAdminToken(admin);
    String rawRefreshToken = generateAndSaveRefreshTokenForAdmin(admin);
    log.info("Platform admin {} logged in successfully", admin.getEmail());
    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(rawRefreshToken)
        .expiresIn(jwtUtil.getAccessTokenExpiryMs() / 1000)
        .passwordChangeRequired(false)
        .build();
  }

  // Then check tenant users table
  User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
      .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

  if (!"ACTIVE".equals(user.getTenant().getStatus())) {
    throw new org.springframework.security.access.AccessDeniedException("Tenant account is not active");
  }

  if (loginAttemptService.isBlocked(user.getEmail())) {
    throw new InvalidCredentialsException("Account is temporarily locked due to too many failed attempts.");
  }

  if (!user.isAccountNonLocked()) {
    throw new InvalidCredentialsException(
        "Account is temporarily locked by administrator. Please try again in " + LOCKOUT_MINUTES + " minutes.");
  }

  if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    loginAttemptService.loginFailed(user.getEmail());
    handleFailedLogin(user); // Also update DB if needed, though redundant now
    throw new InvalidCredentialsException("Invalid email or password");
  }

  loginAttemptService.loginSucceeded(user.getEmail());

  user.setFailedLoginAttempts(0);
  user.setLockedUntil(null);
  userRepository.save(user);

  String accessToken = jwtUtil.generateAccessToken(user);
  String rawRefreshToken = generateAndSaveRefreshToken(user);
  log.info("User {} logged in successfully for tenant {}", user.getEmail(), user.getTenantId());

  return LoginResponse.builder()
      .accessToken(accessToken)
      .refreshToken(rawRefreshToken)
      .expiresIn(jwtUtil.getAccessTokenExpiryMs() / 1000)
      .passwordChangeRequired(user.isPasswordChangeRequired())
      .build();
}

    @Transactional(rollbackFor = Exception.class)
  public LoginResponse refresh(TokenRefreshRequest request) {
    String tokenHash = sha256(request.getRefreshToken());

    RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired refresh token"));

    if (!storedToken.isValid()) {
      throw new InvalidCredentialsException("Refresh token has expired or been revoked");
    }

    // NEW LOGIC: Check if this token belongs to a Super Admin first!
    if (storedToken.getPlatformAdminId() != null) {
      PlatformAdmin admin = platformAdminRepository.findById(storedToken.getPlatformAdminId())
          .orElseThrow(() -> new InvalidCredentialsException("Admin not found"));
      
      String newAccessToken = jwtUtil.generateAdminToken(admin);
      
      return LoginResponse.builder()
          .accessToken(newAccessToken)
          .refreshToken(request.getRefreshToken())
          .expiresIn(jwtUtil.getAccessTokenExpiryMs() / 1000)
          .passwordChangeRequired(false)
          .build();
    }

    // ORIGINAL LOGIC: Fallback to regular Tenant User
    User user = storedToken.getUser();
    String newAccessToken = jwtUtil.generateAccessToken(user);

    return LoginResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(request.getRefreshToken()) 
        .expiresIn(jwtUtil.getAccessTokenExpiryMs() / 1000)
        .passwordChangeRequired(user.isPasswordChangeRequired())
        .build();
  }


  /**
   * Revokes the given refresh token and blacklists the access token.
   */
  @Transactional(rollbackFor = Exception.class)
  public void logout(String rawRefreshToken, @org.jspecify.annotations.Nullable String accessToken) {
    String tokenHash = sha256(rawRefreshToken);
    refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
      token.setRevokedAt(OffsetDateTime.now());
      refreshTokenRepository.save(token);
    });

    if (accessToken != null && jwtUtil.isTokenValid(accessToken)) {
      long timeToLive = jwtUtil.getRemainingTimeInMillis(accessToken);
      if (timeToLive > 0) {
        tokenBlacklistService.blacklistToken(accessToken, timeToLive);
      }
    }
  }

  /**
   * Changes the authenticated user's password.
   * Revokes ALL existing refresh tokens to force re-login on all devices.
   */
  @Transactional(rollbackFor = Exception.class)
  public void changePassword(com.spiceflow.backend.auth.dto.AuthenticatedUser currentUser, ChangePasswordRequest request) {
    User user = userRepository.findById(currentUser.getId())
        .orElseThrow(() -> new com.spiceflow.backend.common.exception.ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
      throw new com.spiceflow.backend.common.exception.BusinessRuleViolationException("Current password is incorrect");
    }

    user.setPasswordHash(java.util.Objects.requireNonNull(passwordEncoder.encode(request.getNewPassword())));
    user.setPasswordChangeRequired(false);
    userRepository.save(user);

    // Invalidate all sessions — user must log in again on all devices
    refreshTokenRepository.revokeAllByUserId(user.getId(), java.time.OffsetDateTime.now());
    log.info("Password changed for user {}. All sessions revoked.", user.getEmail());
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private void handleFailedLogin(User user) {
    int attempts = user.getFailedLoginAttempts() + 1;
    user.setFailedLoginAttempts(attempts);
    if (attempts >= MAX_FAILED_ATTEMPTS) {
      user.setLockedUntil(OffsetDateTime.now().plusMinutes(LOCKOUT_MINUTES));
      log.warn("Account locked for user {} after {} failed attempts",
          user.getEmail(), attempts);
    }
    userRepository.save(user);
  }

  private String generateAndSaveRefreshToken(User user) {
    String rawToken = UUID.randomUUID().toString();
    String tokenHash = sha256(rawToken);

    RefreshToken refreshToken = RefreshToken.builder()
        .user(user)
        .tokenHash(tokenHash)
        .expiresAt(OffsetDateTime.now().plusSeconds(
            jwtUtil.getRefreshTokenExpiryMs() / 1000))
        .build();

    refreshTokenRepository.save(refreshToken);
    return rawToken;
  }

  private String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private String generateAndSaveRefreshTokenForAdmin(PlatformAdmin admin) {
    String rawToken = UUID.randomUUID().toString();
    String tokenHash = sha256(rawToken);
    RefreshToken refreshToken = RefreshToken.builder()
        .platformAdminId(admin.getId())
        .tokenHash(tokenHash)
        .expiresAt(OffsetDateTime.now().plusSeconds(
            jwtUtil.getRefreshTokenExpiryMs() / 1000))
        .build();
    refreshTokenRepository.save(refreshToken);
    return rawToken;
  }
}


