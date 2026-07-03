package com.spiceflow.backend.admin.dto.request;
import lombok.Builder;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Builder
public record CreateTenantRequest(


    @Schema(description = "The registered name of the business", example = "Spice Flow Inc.")
    @NotBlank(message = "Business name is required")
    String businessName,

    @Schema(description = "The ID of the business type", example = "1")
    @NotNull(message = "Business type ID is required")
    Long businessTypeId,

    @Schema(description = "The email address of the business owner (used for initial login)", example = "owner@spiceflow.com")
    @NotBlank(message = "Owner email is required")
    @Email(message = "Invalid email format")
    String ownerEmail,

    @Schema(description = "The initial password for the owner account", example = "SecurePass123!")
    @NotBlank(message = "Owner password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String ownerPassword



) {}