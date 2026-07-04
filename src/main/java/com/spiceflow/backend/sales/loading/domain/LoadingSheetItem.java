package com.spiceflow.backend.sales.loading.domain;

import org.jspecify.annotations.Nullable;

/**
 * Immutable record representing a line item loaded onto a van in a Loading Sheet.
 */
public record LoadingSheetItem(
        @Nullable Long id,
        Long productId,
        Integer quantityLoaded,
        @Nullable String unitType
) {
    public LoadingSheetItem {
        if (productId == null) throw new IllegalArgumentException("productId cannot be null");
        if (quantityLoaded == null || quantityLoaded < 0) throw new IllegalArgumentException("quantityLoaded cannot be negative");
    }

    public LoadingSheetItem(Long productId, Integer quantityLoaded, @Nullable String unitType) {
        this(null, productId, quantityLoaded, unitType);
    }
}
