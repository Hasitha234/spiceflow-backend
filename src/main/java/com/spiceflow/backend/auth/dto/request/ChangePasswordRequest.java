package com.spiceflow.backend.auth.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request body for PUT /api/v1/auth/change-password */
@Schema(description = "Request payload for changing user password")
@Builder
public record ChangePasswordRequest(

    
    @NotBlank(message = "Current password is required")
    @Schema(description = "The user's current password", example = "OldP@ssw0rd")
    String currentPassword,

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New Password must be at least 8 characters")
    @Schema(description = "The new password to set", example = "NewP@ssw0rd123!")
    String newPassword,

    @NotBlank(message = "Confirm new password is required")
    @Size(min = 8, message = "Confirm New Password must be at least 8 characters")
    @Schema(description = "Confirmation of the new password", example = "NewP@ssw0rd123!")
    String confirmNewPassword



) {}