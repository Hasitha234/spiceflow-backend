package com.spiceflow.backend.auth.dto.response;
import lombok.Builder;


import io.swagger.v3.oas.annotations.media.Schema;

/** Response body returned after sucessful login or token refresh. */
@Schema(description = "Response payload containing authentication tokens")
@Builder
public record LoginResponse(


    @Schema(description = "JWT access token for authenticating API requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,

    @Schema(description = "JWT refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken,

    @Schema(description = "Token type prefix", example = "Bearer")
    String tokenType,

    @Schema(description = "Number of seconds until the access token expires", example = "3600")
    Long expiresIn,

    @Schema(description = "Flag indicating if the user must change their password before proceeding", example = "false")
    boolean passwordChangeRequired



) {}