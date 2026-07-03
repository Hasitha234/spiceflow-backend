package com.spiceflow.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request body for PUT /api/v1/auth/change-password */
@Getter
@NoArgsConstructor
@Schema(description = "Request payload for changing user password")
@SuppressWarnings("NullAway.Init")
public class ChangePasswordRequest {
    
    @NotBlank(message = "Current password is required")
    @Schema(description = "The user's current password", example = "OldP@ssw0rd")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New Password must be at least 8 characters")
    @Schema(description = "The new password to set", example = "NewP@ssw0rd123!")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required")
    @Size(min = 8, message = "Confirm New Password must be at least 8 characters")
    @Schema(description = "Confirmation of the new password", example = "NewP@ssw0rd123!")
    private String confirmNewPassword;
}

