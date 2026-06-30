package com.spiceflow.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request body for PUT /api/v1/auth/change-password */
@Getter
@NoArgsConstructor
public class ChangePasswordRequest {
    
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New Password must be at least 8 characters")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required")
    @Size(min = 8, message = "Confirm New Password must be at least 8 characters")
    private String confirmNewPassword;
}
