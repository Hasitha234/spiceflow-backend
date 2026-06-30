package com.spiceflow.backend.admin.dto.request;

import com.spiceflow.backend.common.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreateTenantRequest {

    @Schema(description = "The registered name of the business", example = "Spice Flow Inc.")
    @NotBlank(message = "Business name is required")
    private String businessName;

    @Schema(description = "The type of the business", example = "MANUFACTURER")
    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    @Schema(description = "The email address of the business owner (used for initial login)", example = "owner@spiceflow.com")
    @NotBlank(message = "Owner email is required")
    @Email(message = "Invalid email format")
    private String ownerEmail;

    @Schema(description = "The initial password for the owner account", example = "SecurePass123!")
    @NotBlank(message = "Owner password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String ownerPassword;
}
