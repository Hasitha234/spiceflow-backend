package com.spiceflow.backend.inventory.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Builder
public record InventoryItemRequest(

    
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @NotNull(message = "Warehouse ID is required")
    Long warehouseId,
    
    @NotNull(message = "Quantity available is required")
    @Min(value = 0, message = "Quantity available must be zero or greater")
    Integer quantityAvailable,
    
    @NotNull(message = "Quantity reserved is required")
    @Min(value = 0, message = "Quantity reserved must be zero or greater")
    Integer quantityReserved,
    
    String batchNumber,
    
    LocalDate expirationDate



) {}