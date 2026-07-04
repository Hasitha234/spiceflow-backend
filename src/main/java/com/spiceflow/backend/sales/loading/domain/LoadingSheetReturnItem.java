package com.spiceflow.backend.sales.loading.domain;

import org.jspecify.annotations.Nullable;

/**
 * Immutable record representing an expected return item in a Loading Sheet.
 */
public record LoadingSheetReturnItem(
        @Nullable Long id,
        Long productId,
        Integer quantityReturned,
        @Nullable String unitType,
        String returnType
) {
    public LoadingSheetReturnItem {
        if (productId == null) throw new IllegalArgumentException("productId cannot be null");
        if (quantityReturned == null || quantityReturned < 0) throw new IllegalArgumentException("quantityReturned cannot be negative");
        if (returnType == null || returnType.isBlank()) throw new IllegalArgumentException("returnType cannot be blank");
    }

    public LoadingSheetReturnItem(Long productId, Integer quantityReturned, @Nullable String unitType, String returnType) {
        this(null, productId, quantityReturned, unitType, returnType);
    }
}
