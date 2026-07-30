package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MorningSummaryRequest(
    @NotNull(message = "Rep ID is required")
    Long repId,
    
    @NotNull(message = "Driver ID is required")
    Long driverId,
    
    @NotNull(message = "Summary Date is required")
    LocalDate summaryDate,
    
    @NotNull(message = "Items are required")
    List<MorningSummaryItemRequest> items
) {
    public record MorningSummaryItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,
        
        @NotNull(message = "Quantity is required")
        Integer quantity,
        
        Integer expectedReturnAmount,
        BigDecimal expectedReturnPrice
    ) {}
}
