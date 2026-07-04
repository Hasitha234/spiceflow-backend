package com.spiceflow.backend.sales.delivery.domain;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Immutable record representing a delivered line item for a shop within a Delivery.
 * ADR-013: Self-validating, NullAway-safe, no setters.
 */
public record DeliveryShopItemRecord(
        @Nullable Long id,
        Long productId,
        Integer quantityDelivered,
        @Nullable String unitType,
        BigDecimal rate,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal netAmount,
        boolean isFreeItem
) {
    public DeliveryShopItemRecord {
        if (productId == null) throw new IllegalArgumentException("productId cannot be null");
        if (quantityDelivered == null || quantityDelivered < 0) throw new IllegalArgumentException("quantityDelivered cannot be negative");
        if (rate == null) throw new IllegalArgumentException("rate cannot be null");
        if (grossAmount == null) throw new IllegalArgumentException("grossAmount cannot be null");
        if (discountAmount == null) throw new IllegalArgumentException("discountAmount cannot be null");
        if (netAmount == null) throw new IllegalArgumentException("netAmount cannot be null");
    }

    /** Convenience constructor without a persisted id. */
    public DeliveryShopItemRecord(Long productId, Integer quantityDelivered, @Nullable String unitType,
                                   BigDecimal rate, BigDecimal grossAmount, BigDecimal discountAmount,
                                   BigDecimal netAmount, boolean isFreeItem) {
        this(null, productId, quantityDelivered, unitType, rate, grossAmount, discountAmount, netAmount, isFreeItem);
    }
}
