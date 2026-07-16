package com.spiceflow.backend.admin.dto.request;

import lombok.Builder;
import java.util.List;

@Builder
public record UpdateUserRequest(
    String name,
    String userType,
    Long tenantId,
    List<Long> tenantIds
) {}
