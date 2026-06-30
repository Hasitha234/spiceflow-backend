package com.spiceflow.backend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

/** Response body returned after sucessful login or token refresh. */

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;
    private boolean passwordChangeRequired;

}
