package com.spiceflow.backend.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InventoryTransactionRequest {
    
    @NotNull(message = "Inventory item ID is required")
    private Long inventoryItemId;
    
    @NotBlank(message = "Transaction type is required")
    private String transactionType; // IN, OUT, ADJUST, RESERVE, RELEASE
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
    
    private String referenceId;
    
    private String notes;
}
