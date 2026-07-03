package com.spiceflow.backend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response body returned after sucessful login or token refresh. */
@Getter
@Builder
@Schema(description = "Response payload containing authentication tokens")
public class LoginResponse {

    @Schema(description = "JWT access token for authenticating API requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "JWT refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Token type prefix", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Number of seconds until the access token expires", example = "3600")
    private Long expiresIn;

    @Schema(description = "Flag indicating if the user must change their password before proceeding", example = "false")
    private boolean passwordChangeRequired;

}
