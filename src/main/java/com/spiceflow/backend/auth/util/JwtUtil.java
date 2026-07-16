package com.spiceflow.backend.auth.util;

import com.spiceflow.backend.admin.entity.PlatformAdmin;
import com.spiceflow.backend.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Utility for generating and validating JWTs. Stateless — no DB calls. */
@SuppressWarnings("JavaUtilDate")
@Component
public class JwtUtil {

  private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

  private static final String CLAIM_TENANT_ID = "tenantId";
  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_ROLES = "roles";
  private static final String CLAIM_PERMISSIONS = "permissions";

  private final SecretKey secretKey;
  private final long accessTokenExpiryMs;
  private final long refreshTokenExpiryMs;
  private static final String CLAIM_USER_TYPE = "userType";


  public JwtUtil(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-token-expiry}") long accessTokenExpiryMs,
      @Value("${app.jwt.refresh-token-expiry}") long refreshTokenExpiryMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpiryMs = accessTokenExpiryMs;
    this.refreshTokenExpiryMs = refreshTokenExpiryMs;
  }

  /** Generates a signed access JWT for the given user. Embeds tenantId, userId, roles and permissions. */
  public String generateAccessToken(User user, java.util.List<Object> assignedTenants) {
    long now = System.currentTimeMillis();
    
    List<String> roles = List.of();
    List<String> permissions = List.of();
    
    if (user.getAssignedRole() != null) {
      String roleName = user.getAssignedRole().getName().toUpperCase(java.util.Locale.ROOT).replace(" ", "_");
      String roleAuth = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
      roles = List.of(roleAuth);
      
      if (user.getAssignedRole().getPermissions() != null) {
        permissions = user.getAssignedRole().getPermissions().stream()
            .map(com.spiceflow.backend.auth.entity.Permission::getCode)
            .collect(Collectors.toList());
      }
    }

    return Jwts.builder()
        .subject(user.getEmail())
        .claim(CLAIM_USER_TYPE, user.getUserType())
        .claim(CLAIM_TENANT_ID, user.getTenantId())
        .claim("tenantStatus", user.getTenant() != null ? user.getTenant().getStatus() : null)
        .claim("assignedTenants", assignedTenants != null ? assignedTenants : List.of())
        .claim(CLAIM_USER_ID, user.getId())
        .claim(CLAIM_ROLE, user.getAssignedRole() != null ? user.getAssignedRole().getName() : null)
        .claim(CLAIM_ROLES, roles)
        .claim(CLAIM_PERMISSIONS, permissions)
        .issuedAt(new Date(now))
        .expiration(new Date(now + accessTokenExpiryMs))
        .signWith(secretKey)
        .compact();
  }

  /** Backwards compatibility signature for tests/other callers */
  public String generateAccessToken(User user) {
      return generateAccessToken(user, List.of());
  }


  /** Generates a signed access JWT for a platform admin. Has no tenantId. */
  public String generateAdminToken(PlatformAdmin admin) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .subject(admin.getEmail())
        .claim(CLAIM_USER_TYPE, "PLATFORM_ADMIN")
        .claim(CLAIM_USER_ID, admin.getId())
        .claim(CLAIM_ROLES, List.of("ROLE_SUPER_ADMIN"))
        .claim(CLAIM_PERMISSIONS, List.of())
        .issuedAt(new Date(now))
        .expiration(new Date(now + accessTokenExpiryMs))
        .signWith(secretKey)
        .compact();
  }


  /** Extracts the email (subject) from a JWT. Returns null if token is invalid. */
  public @org.jspecify.annotations.Nullable String extractEmail(String token) {
    try {
      return parseClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
      log.warn("Could not extract email from token: {}", e.getMessage(), e);
      return null;
    }

  }

  /** Extracts the tenantId claim from a JWT. Returns null if token is invalid. */
  public @org.jspecify.annotations.Nullable Long extractTenantId(String token) {
    try {
      Object tenantId = parseClaims(token).get(CLAIM_TENANT_ID);
      return tenantId != null ? Long.valueOf(tenantId.toString()) : null;
        } catch (JwtException | IllegalArgumentException e) {
      log.warn("Could not extract tenantId from token: {}", e.getMessage(), e);
      return null;
    }

  }

  /** Extracts the userType claim. Returns null if token is invalid. */
public @org.jspecify.annotations.Nullable String extractUserType(String token) {
  try {
    Object userType = parseClaims(token).get(CLAIM_USER_TYPE);
    return userType != null ? userType.toString() : null;
      } catch (JwtException | IllegalArgumentException e) {
      log.warn("Could not extract userType from token: {}", e.getMessage(), e);
      return null;
    }

}


  /** Returns true if the token has a valid signature and is not expired. */
  public boolean isTokenValid(String token) {
    try {
      parseClaims(token);
      return true;
        } catch (JwtException | IllegalArgumentException e) {
      log.warn("Token validation failed: {}", e.getMessage(), e);
      return false;
    }

  }

  /** Returns the remaining time in milliseconds before the token expires. */
  public long getRemainingTimeInMillis(String token) {
    try {
      Date expiration = parseClaims(token).getExpiration();
      long remaining = expiration.getTime() - System.currentTimeMillis();
      return Math.max(0, remaining);
    } catch (JwtException | IllegalArgumentException e) {
      return 0;
    }
  }

  /** Returns the configured access token expiry in milliseconds. */
  public long getAccessTokenExpiryMs() {
    return accessTokenExpiryMs;
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public long getRefreshTokenExpiryMs() {
  return refreshTokenExpiryMs;
}

}

