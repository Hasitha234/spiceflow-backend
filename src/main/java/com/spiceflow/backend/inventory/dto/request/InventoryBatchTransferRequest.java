package com.spiceflow.backend.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record InventoryBatchTransferRequest(
    @NotNull(message = "Source warehouse ID is required") Long fromWarehouseId,
    @NotNull(message = "Destination warehouse ID is required") Long toWarehouseId,
    @NotEmpty(message = "Transfer items cannot be empty") @Valid List<TransferLineItem> items,
    String notes
) {
    public record TransferLineItem(
        @NotNull(message = "Product ID is required") Long productId,
        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") Integer quantity
    ) {}
}
