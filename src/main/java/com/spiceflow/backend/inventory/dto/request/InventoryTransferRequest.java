package com.spiceflow.backend.inventory.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Builder
public record InventoryTransferRequest(

    
    @NotNull(message = "Source warehouse ID is required")
    Long fromWarehouseId,
    
    @NotNull(message = "Destination warehouse ID is required")
    Long toWarehouseId,
    
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Integer quantity,
    
    String reason



) {}