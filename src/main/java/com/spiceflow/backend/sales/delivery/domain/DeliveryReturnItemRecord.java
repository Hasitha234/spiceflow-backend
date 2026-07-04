package com.spiceflow.backend.sales.delivery.domain;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Immutable record representing a product returned from a shop during a Delivery.
 * ADR-013: Self-validating, NullAway-safe, no setters.
 * returnType: EXPIRED | DAMAGED | GOOD
 */
public record DeliveryReturnItemRecord(
        @Nullable Long id,
        Long productId,
        Integer quantityReturned,
        @Nullable String unitType,
        BigDecimal creditValue,
        String returnType
) {
    public DeliveryReturnItemRecord {
        if (productId == null) throw new IllegalArgumentException("productId cannot be null");
        if (quantityReturned == null || quantityReturned < 0) throw new IllegalArgumentException("quantityReturned cannot be negative");
        if (creditValue == null) throw new IllegalArgumentException("creditValue cannot be null");
        if (returnType == null || returnType.isBlank()) throw new IllegalArgumentException("returnType cannot be blank");
    }

    /** Convenience constructor without a persisted id. */
    public DeliveryReturnItemRecord(Long productId, Integer quantityReturned, @Nullable String unitType,
                                     BigDecimal creditValue, String returnType) {
        this(null, productId, quantityReturned, unitType, creditValue, returnType);
    }
}
