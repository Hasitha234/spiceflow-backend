package com.spiceflow.backend.admin.dto.response;
import lombok.Builder;


import java.time.OffsetDateTime;

@Builder
public record TenantResponse(

    Long id,
    String businessName,
    Long businessTypeId,
    String businessTypeName,
    String email,
    String status,
    String plan,
    OffsetDateTime createdAt



) {}