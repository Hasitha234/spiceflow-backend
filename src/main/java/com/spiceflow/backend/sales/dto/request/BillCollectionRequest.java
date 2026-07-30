package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BillCollectionRequest(
        @NotNull(message = "Cash Collected is required")
        @Min(value = 0, message = "Cash Collected cannot be negative")
        BigDecimal cashCollected,

        @NotNull(message = "Check Collected is required")
        @Min(value = 0, message = "Check Collected cannot be negative")
        BigDecimal checkCollected,

        @NotNull(message = "Loan Amount is required")
        @Min(value = 0, message = "Loan Amount cannot be negative")
        BigDecimal loanAmount,

        @org.jspecify.annotations.Nullable LocalDate loanDueDate
) {}
