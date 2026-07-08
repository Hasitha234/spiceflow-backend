package com.spiceflow.backend.sales.order.domain;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/*
 * Immutable record representing a line item in a sales representative's order.
 * All financial amounts and quantities are preserved as immutable snapshots.
 */
public record RepOrderItem(
        @Nullable Long id,
        Long productId,
        Integer quantity,
        @Nullable String unitType,
        BigDecimal rate,
        @Nullable BigDecimal grossAmount,
        @Nullable BigDecimal netAmount,
        Boolean isFreeItem,
        Integer boxesNeeded
) {
    public RepOrderItem {
        if (productId == null) throw new IllegalArgumentException("productId cannot be null");
        if (quantity == null || quantity < 0) throw new IllegalArgumentException("quantity cannot be negative");
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("rate cannot be negative");
        if (isFreeItem == null) isFreeItem = false;
        if (boxesNeeded == null || boxesNeeded < 0) boxesNeeded = 0;
    }

    public RepOrderItem(Long productId, Integer quantity, @Nullable String unitType, BigDecimal rate, Boolean isFreeItem, Integer boxesNeeded) {
        this(
                null,
                productId,
                quantity,
                unitType,
                rate,
                rate != null && quantity != null ? rate.multiply(new BigDecimal(quantity)) : BigDecimal.ZERO,
                rate != null && quantity != null ? rate.multiply(new BigDecimal(quantity)) : BigDecimal.ZERO,
                isFreeItem,
                boxesNeeded
        );
    }
}
