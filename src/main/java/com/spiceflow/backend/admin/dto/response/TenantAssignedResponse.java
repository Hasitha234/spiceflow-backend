package com.spiceflow.backend.admin.dto.response;

import lombok.Builder;

@Builder
public record TenantAssignedResponse(
    Long id,
    String businessName,
    String status
) {}
