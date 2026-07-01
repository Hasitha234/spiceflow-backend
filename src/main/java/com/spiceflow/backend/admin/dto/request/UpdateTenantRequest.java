package com.spiceflow.backend.admin.dto.request;

import com.spiceflow.backend.common.enums.BusinessType;
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

    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Plan is required")
    private String plan;
}
