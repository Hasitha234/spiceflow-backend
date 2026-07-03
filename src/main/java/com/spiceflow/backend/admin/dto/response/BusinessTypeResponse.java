package com.spiceflow.backend.admin.dto.response;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record BusinessTypeResponse(

    Long id,
    String name,
    String description,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt



) {}