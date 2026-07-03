package com.spiceflow.backend.auth.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request body for POST /api/v1/auth/refresh */
@Getter
@NoArgsConstructor
@Schema(description = "Request payload for refreshing an access token")
@SuppressWarnings("NullAway.Init")
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "The valid refresh token obtained during login", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}

