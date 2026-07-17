package com.spiceflow.backend.admin.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record UserResponse(
    Long id,
    String name,
    String email,
    String userType,
    Long tenantId,
    String tenantName,
    String roleName,
    List<TenantAssignedResponse> assignedTenants,
    OffsetDateTime createdAt
) {}
