package com.spiceflow.backend.sales.order.domain;

import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Immutable return item representing expired or damaged goods returned by a shop.
 * Rule 15 compliant: no setter mutations; all state is frozen upon construction.
 */
public record ShopReturnItem(
        @Nullable Long id,
        Long productId,
        Integer quantity,
        @Nullable String unitType,
        BigDecimal creditValue,
        String returnType,
        @Nullable String status
) {
    public ShopReturnItem {
        Objects.requireNonNull(productId, "productId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(creditValue, "creditValue cannot be null");
        Objects.requireNonNull(returnType, "returnType cannot be null");
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        if (creditValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("creditValue cannot be negative");
        }
        status = status != null ? status : "PENDING";
    }

    public ShopReturnItem(Long productId, Integer quantity, String unitType, BigDecimal creditValue,
                          String returnType) {
        this(null, productId, quantity, unitType, creditValue, returnType, "PENDING");
    }
}
