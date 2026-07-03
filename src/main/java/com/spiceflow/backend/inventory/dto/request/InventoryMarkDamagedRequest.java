package com.spiceflow.backend.inventory.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Builder
public record InventoryMarkDamagedRequest(

    
    @NotNull(message = "Warehouse ID is required")
    Long warehouseId,
    
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Integer quantity,
    
    String notes



) {}