package com.spiceflow.backend.auth.dto.request;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request body for POST /api/v1/auth/refresh */
@Schema(description = "Request payload for refreshing an access token")
@Builder
public record TokenRefreshRequest(


    @NotBlank(message = "Refresh token is required")
    @Schema(description = "The valid refresh token obtained during login", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken



) {}