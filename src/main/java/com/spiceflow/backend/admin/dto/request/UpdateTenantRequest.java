package com.spiceflow.backend.admin.dto.request;
import lombok.Builder;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for PUT /api/v1/admin/tenants/{id} */
@Builder
public record UpdateTenantRequest(


    @NotBlank(message = "Business name is required")
    String businessName,

    @Schema(description = "The ID of the business type", example = "1")
    Long businessTypeId,

    @NotBlank(message = "Status is required")
    String status,

    @NotBlank(message = "Plan is required")
    String plan



) {}