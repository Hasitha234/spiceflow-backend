package com.spiceflow.backend.sales.dto.response;

import java.util.List;

public record DeductInventoryPreCheckResponse(
    boolean canDeduct,
    List<ItemAvailability> items
) {
    public record ItemAvailability(
        Long productId,
        String productName,
        int requiredQuantity,
        int availableQuantity,
        boolean sufficient,
        int expectedReturnQuantity
    ) {}
}
