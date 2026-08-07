package com.spiceflow.backend.inventory.dto.response;

import java.time.Instant;
import java.util.List;

public record InventoryBatchTransferResponse(
    String referenceNumber,
    Long fromWarehouseId,
    String fromWarehouseName,
    Long toWarehouseId,
    String toWarehouseName,
    List<TransferItemDetail> transferredItems,
    int totalItems,
    int totalQuantity,
    Instant timestamp
) {
    public record TransferItemDetail(
        Long productId,
        String productName,
        String productSku,
        Integer quantity
    ) {}
}
