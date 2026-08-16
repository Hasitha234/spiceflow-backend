package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EveningSummaryItemRequest(
    @NotNull(message = "Product ID is required")
    Long productId,

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    Integer quantity,

    @NotNull(message = "Unit price is required")
    @Min(value = 0, message = "Unit price cannot be negative")
    BigDecimal unitPrice,

    @NotNull(message = "Estimate value is required")
    @Min(value = 0, message = "Estimate value cannot be negative")
    BigDecimal estimateValue
) {}
