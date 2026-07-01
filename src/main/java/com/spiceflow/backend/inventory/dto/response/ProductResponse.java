package com.spiceflow.backend.inventory.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String unitOfMeasure;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
