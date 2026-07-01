package com.spiceflow.backend.inventory.dto.response;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentCategoryId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
