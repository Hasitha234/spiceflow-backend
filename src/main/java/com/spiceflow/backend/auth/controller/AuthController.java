package com.spiceflow.backend.auth.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.request.ChangePasswordRequest;
import com.spiceflow.backend.auth.dto.request.LoginRequest;
import com.spiceflow.backend.auth.dto.response.LoginResponse;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Arrays;
import java.util.Optional;

/** REST endpoints for authentication — login, refresh, logout, change password. */
@RestController
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

  /** Name of the HttpOnly cookie that holds the refresh token. */
  static final String REFRESH_COOKIE_NAME = "refreshToken";

  private final AuthService authService;

  /**
   * Cookie max-age mirrors the refresh token TTL from config (default 30 days in seconds).
   * The value is injected from {@code app.jwt.refresh-token-expiry-days}, which must be
   * configured in application.yml as a number of days.
   */
  @Value("${app.jwt.refresh-token-expiry-days:30}")
  private int refreshTokenExpiryDays;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Build a Secure + HttpOnly + SameSite=Strict cookie containing the refresh token. */
  private ResponseCookie buildRefreshCookie(String rawRefreshToken) {
    return ResponseCookie.from(REFRESH_COOKIE_NAME, rawRefreshToken)
        .httpOnly(true)
        .secure(true)           // requires HTTPS in prod; set to false in local profile override
        .path("/api/v1/auth")   // scoped to auth endpoints only, not the whole API
        .maxAge((long) refreshTokenExpiryDays * 24 * 60 * 60)
        .sameSite("Strict")
        .build();
  }

  /** Clear the refresh token cookie (used during logout). */
  private ResponseCookie clearRefreshCookie() {
    return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(true)
        .path("/api/v1/auth")
        .maxAge(0)
        .sameSite("Strict")
        .build();
  }

  /** Extract the raw refresh token from the HttpOnly cookie, if present. */
  private Optional<String> extractRefreshCookie(HttpServletRequest request) {
    if (request.getCookies() == null) return Optional.empty();
    return Arrays.stream(request.getCookies())
        .filter(c -> REFRESH_COOKIE_NAME.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  // ---------------------------------------------------------------------------
  // Endpoints
  // ---------------------------------------------------------------------------

  /**
   * Authenticates a user and returns a JWT access token in the body.
   * The refresh token is set as an HttpOnly cookie — it never appears in the JSON response.
   * Public endpoint — no JWT required.
   */
  @PostMapping("/login")
  @Tag(name = "1. Super Admin Operations")
  @Tag(name = "2. Tenant Owner Operations")
  @Operation(summary = "Unified Login", description = "Authenticates either a Super Admin or a Tenant Owner. Returns JWT access token in body; sets refresh token as an HttpOnly cookie.", operationId = "login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletResponse response) {

    LoginResponse loginResponse = authService.login(request);
    // refreshToken field is @JsonIgnore — safe to read here, never serialised to JSON.
    response.addHeader(HttpHeaders.SET_COOKIE,
        buildRefreshCookie(loginResponse.refreshToken()).toString());
    return ResponseEntity.ok(loginResponse);
  }

  /**
   * Issues a new access token using the HttpOnly refresh cookie.
   * No request body required — the refresh token is read from the cookie.
   * Public endpoint — no JWT required.
   */
  @PostMapping("/refresh")
  @Tag(name = "1. Super Admin Operations")
  @Tag(name = "2. Tenant Owner Operations")
  @Operation(summary = "Refresh Token", description = "Issues a new access token. Reads the refresh token from the HttpOnly cookie; no request body needed.", operationId = "refresh")
  @ApiResponse(responseCode = "200", description = "New access token issued")
  @ApiResponse(responseCode = "401", description = "Cookie missing, invalid, or expired")
  public ResponseEntity<LoginResponse> refresh(
      HttpServletRequest request,
      HttpServletResponse response) {

    String rawRefreshToken = extractRefreshCookie(request)
        .orElseThrow(() ->
            new com.spiceflow.backend.common.exception.InvalidCredentialsException(
                "Refresh token cookie is missing"));

    LoginResponse refreshed = authService.refreshByCookie(rawRefreshToken);
    // Re-issue cookie to extend sliding window (optional; comment out for fixed-expiry tokens)
    response.addHeader(HttpHeaders.SET_COOKIE,
        buildRefreshCookie(refreshed.refreshToken()).toString());
    return ResponseEntity.ok(refreshed);
  }

  /**
   * Revokes the refresh token cookie and blacklists the current access token.
   * Requires a valid JWT in the Authorization header.
   */
  @PostMapping("/logout")
  @Tag(name = "1. Super Admin Operations")
  @Tag(name = "2. Tenant Owner Operations")
  @Operation(summary = "Logout", description = "Revokes the refresh token cookie and invalidates the current access token.", operationId = "logout")
  @ApiResponse(responseCode = "204", description = "Successfully logged out")
  public ResponseEntity<Void> logout(
      HttpServletRequest request,
      HttpServletResponse response) {

    String rawRefreshToken = extractRefreshCookie(request).orElse("");

    String authHeader = request.getHeader("Authorization");
    @org.jspecify.annotations.Nullable String accessToken = null;
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      accessToken = authHeader.substring(7);
    }

    authService.logout(rawRefreshToken, accessToken);
    response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
    return ResponseEntity.noContent().build();
  }

  /**
   * Changes the authenticated user's password and revokes all existing sessions.
   * Requires a valid JWT.
   */
  @PostMapping("/change-password")
  @Tag(name = "2. Tenant Owner Operations")
  @Operation(summary = "Change Password", description = "Changes the authenticated user's password and revokes all existing sessions.", operationId = "changePassword")
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      @AuthenticationPrincipal AuthenticatedUser currentUser) {
    authService.changePassword(currentUser, request);
    return ResponseEntity.noContent().build();
  }
}
