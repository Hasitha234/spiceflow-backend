package com.spiceflow.backend.inventory.dto.response;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Builder
public record InventoryItemResponse(

    Long id,
    Long productId,
    String productName,
    String productSku,
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