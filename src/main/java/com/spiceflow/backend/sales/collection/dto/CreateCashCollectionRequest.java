package com.spiceflow.backend.sales.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Request DTO for creating a new Cash Collection record.
 */
@Builder
public record CreateCashCollectionRequest(
        @NotNull(message = "Shop ID is required")
        Long shopId,

        @Nullable
        Long repId,

        @NotNull(message = "Collection date is required")
        LocalDate collectionDate,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        @Nullable
        String chequeNo,

        @Nullable
        String chequeBankName,

        @Nullable
        LocalDate chequeDate,

        @Nullable
        String notes
) {}
