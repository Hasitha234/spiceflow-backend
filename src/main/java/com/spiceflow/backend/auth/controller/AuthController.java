package com.spiceflow.backend.auth.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.request.ChangePasswordRequest;
import com.spiceflow.backend.auth.dto.request.LoginRequest;
import com.spiceflow.backend.auth.dto.request.TokenRefreshRequest;
import com.spiceflow.backend.auth.dto.response.LoginResponse;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;



/** REST endpoints for authentication — login, refresh, logout, change password. */
@RestController
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * Authenticates a user and returns a JWT access token + refresh token.
   * Public endpoint — no JWT required.
   */
  @PostMapping("/login")
  @Tag(name = "1. Super Admin Operations")
  @Tag(name = "2. Tenant Owner Operations")
  @Operation(summary = "Unified Login", description = "Authenticates either a Super Admin or a Tenant Owner. Returns JWT tokens.")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  /**
   * Issues a new access token using a valid refresh token.
   * Public endpoint — no JWT required.
   */
  @PostMapping("/refresh")
  @Tag(name = "1. Super Admin Operations")
  @Tag(name = "2. Tenant Owner Operations")
  @Operation(summary = "Refresh Token", description = "Issues a new access token using a valid refresh token.")
  public ResponseEntity<LoginResponse> refresh(
      @Valid @RequestBody TokenRefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request));
  }

  /**
   * Revokes the provided refresh token and access token.
   * Requires a valid JWT.
   */
  @PostMapping("/logout")
  @Tag(name = "1. Super Admin Operations")
  @Tag(name = "2. Tenant Owner Operations")
  @Operation(summary = "Logout", description = "Revokes the provided refresh token and invalidates the current access token.")
  @ApiResponse(responseCode = "204", description = "Successfully logged out")
  public ResponseEntity<Void> logout(
      @Valid @RequestBody TokenRefreshRequest request,
      jakarta.servlet.http.HttpServletRequest httpServletRequest) {
    
    String authHeader = httpServletRequest.getHeader("Authorization");
    String accessToken = null;
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      accessToken = authHeader.substring(7);
    }
    
    authService.logout(request.getRefreshToken(), accessToken);
    return ResponseEntity.noContent().build();
  }


  /**
   * Changes the authenticated user's password and revokes all existing sessions.
   * Requires a valid JWT.
   */
  @PostMapping("/change-password")
  @Tag(name = "2. Tenant Owner Operations") 
  @Operation(summary = "Change Password", description = "Changes the authenticated user's password and revokes all existing sessions.")
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      @AuthenticationPrincipal User currentUser) {
    authService.changePassword(currentUser, request);
    return ResponseEntity.noContent().build();
  }
}
