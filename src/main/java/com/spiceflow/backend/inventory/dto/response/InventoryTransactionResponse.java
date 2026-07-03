package com.spiceflow.backend.inventory.dto.response;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record InventoryTransactionResponse(

    Long id,
    Long inventoryItemId,
    String productName,
    String warehouseName,
    String transactionType,
    Integer quantity,
    String referenceId,
    String notes,
    OffsetDateTime createdAt


) {}