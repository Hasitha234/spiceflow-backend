package com.spiceflow.backend.inventory.dto.response;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ProductCategoryResponse(

    Long id,
    String name,
    String description,
    Long parentCategoryId,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt


) {}