package com.spiceflow.backend.admin.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request body for PUT /api/v1/admin/tenants/{id} */
@Getter
@NoArgsConstructor
public class UpdateTenantRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @Schema(description = "The ID of the business type", example = "1")
    private Long businessTypeId;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Plan is required")
    private String plan;
}
