package com.spiceflow.backend.sales.dto.response;

public record StockAvailabilityResponse(
    Long productId,
    String productName,
    Integer soldQuantity,
    Integer availableQuantity,
    Integer shortQuantity,
    boolean sufficient
) {}
