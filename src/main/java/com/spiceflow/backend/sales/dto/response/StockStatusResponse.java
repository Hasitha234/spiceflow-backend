package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;


@Builder
public record StockStatusResponse(

    Long productId,
    String productName,
    String productCode,
    Integer mainStoreQuantity,
    Integer otherStoresQuantity,
    Integer totalQuantity


) {}