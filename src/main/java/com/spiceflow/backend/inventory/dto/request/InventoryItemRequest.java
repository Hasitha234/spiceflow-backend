package com.spiceflow.backend.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class InventoryItemRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;
    
    @NotNull(message = "Quantity available is required")
    @Min(value = 0, message = "Quantity available must be zero or greater")
    private Integer quantityAvailable;
    
    @NotNull(message = "Quantity reserved is required")
    @Min(value = 0, message = "Quantity reserved must be zero or greater")
    private Integer quantityReserved;
    
    private String batchNumber;
    
    private LocalDate expirationDate;
}

