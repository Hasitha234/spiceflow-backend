package com.spiceflow.backend.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request body for POST /api/v1/auth/login. */
@Getter
@NoArgsConstructor
@Schema(description = "Request payload for user authentication")
@SuppressWarnings("NullAway.Init")
public class LoginRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  @Schema(description = "User's email address", example = "admin@spiceflow.com")
  private String email;

  @NotBlank(message = "Password is required")
  @Schema(description = "User's password", example = "P@ssw0rd123!")
  private String password;
}

