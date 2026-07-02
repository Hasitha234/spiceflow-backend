package com.spiceflow.backend.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InventoryTransferRequest {
    
    @NotNull(message = "Source warehouse ID is required")
    private Long fromWarehouseId;
    
    @NotNull(message = "Destination warehouse ID is required")
    private Long toWarehouseId;
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    private String reason;
}
