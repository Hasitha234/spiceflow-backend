package com.spiceflow.backend.auth.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request body for POST /api/v1/auth/login. */
@Schema(description = "Request payload for user authentication")
@Builder
public record LoginRequest(


  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  @Schema(description = "User's email address", example = "admin@spiceflow.com")
  String email,

  @NotBlank(message = "Password is required")
  @Schema(description = "User's password", example = "P@ssw0rd123!")
  String password



) {}