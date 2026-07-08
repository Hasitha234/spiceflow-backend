package com.spiceflow.backend.inventory.dto.response;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Builder
public record InventoryItemResponse(

    Long id,
    Long productId,
    String productName,
    String productSku,
    String productCategoryName,
    BigDecimal productBasePrice,
    String unitOfMeasure,
    Integer itemsPerSoldUnit,
    Integer soldUnitsPerBox,
    Long warehouseId,
    String warehouseName,
    Integer quantityAvailable,
    Integer quantityReserved,
    String batchNumber,
    LocalDate expirationDate,
    Long version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt

) {}