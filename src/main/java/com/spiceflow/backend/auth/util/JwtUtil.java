package com.spiceflow.backend.auth.util;

import com.spiceflow.backend.admin.entity.PlatformAdmin;
import com.spiceflow.backend.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Utility for generating and validating JWTs. Stateless — no DB calls. */
@Component
public class JwtUtil {

  private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

  private static final String CLAIM_TENANT_ID = "tenantId";
  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_ROLE = "role";

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

  /** Generates a signed access JWT for the given user. Embeds tenantId, userId and role. */
  public String generateAccessToken(User user) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .subject(user.getEmail())
        .claim(CLAIM_USER_TYPE, "TENANT_USER")
        .claim(CLAIM_TENANT_ID, user.getTenantId())
        .claim(CLAIM_USER_ID, user.getId())
        .claim(CLAIM_ROLE, user.getAssignedRole() != null ? user.getAssignedRole().getName() : null)
        .issuedAt(new Date(now))
        .expiration(new Date(now + accessTokenExpiryMs))
        .signWith(secretKey)
        .compact();
  }


  /** Generates a signed access JWT for a platform admin. Has no tenantId. */
public String generateAdminToken(PlatformAdmin admin) {
  long now = System.currentTimeMillis();
  return Jwts.builder()
      .subject(admin.getEmail())
      .claim(CLAIM_USER_TYPE, "PLATFORM_ADMIN")
      .claim(CLAIM_USER_ID, admin.getId())
      .issuedAt(new Date(now))
      .expiration(new Date(now + accessTokenExpiryMs))
      .signWith(secretKey)
      .compact();
}


  /** Extracts the email (subject) from a JWT. Returns null if token is invalid. */
  public String extractEmail(String token) {
    try {
      return parseClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
      log.warn("Could not extract email from token: {}", e.getMessage(), e);
      return null;
    }

  }

  /** Extracts the tenantId claim from a JWT. Returns null if token is invalid. */
  public Long extractTenantId(String token) {
    try {
      Object tenantId = parseClaims(token).get(CLAIM_TENANT_ID);
      return tenantId != null ? Long.valueOf(tenantId.toString()) : null;
        } catch (JwtException | IllegalArgumentException e) {
      log.warn("Could not extract tenantId from token: {}", e.getMessage(), e);
      return null;
    }

  }

  /** Extracts the userType claim. Returns null if token is invalid. */
public String extractUserType(String token) {
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

  private Claims parseClaims(String token) {
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
