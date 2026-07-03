package com.spiceflow.backend.inventory.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record ProductResponse(

    Long id,
    String sku,
    String name,
    String description,
    BigDecimal basePrice,
    String unitOfMeasure,
    Long categoryId,
    String categoryName,
    Long supplierId,
    String supplierName,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    
    String netWeight,
    String unitType,
    String boxConfiguration,
    Integer itemsPerSoldUnit,
    Integer soldUnitsPerBox,
    BigDecimal ratePerSoldUnit


) {}