package com.spiceflow.backend.inventory.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Builder
public record InventoryTransactionRequest(

    
    @NotNull(message = "Inventory item ID is required")
    Long inventoryItemId,
    
    @NotBlank(message = "Transaction type is required")
    String transactionType, // IN, OUT, ADJUST, RESERVE, RELEASE
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    Integer quantity,
    
    String referenceId,
    
    String notes


) {}