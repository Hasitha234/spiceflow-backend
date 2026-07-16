package com.spiceflow.backend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateTenantStatusRequest(
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|SUSPENDED)$", message = "Status must be ACTIVE or SUSPENDED")
    String status
) {}
