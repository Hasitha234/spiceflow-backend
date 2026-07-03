package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record RepResponse(

    Long id,
    String name,
    String phone,
    String area,
    Boolean isActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt


) {}