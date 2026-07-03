package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;


@Builder
public record LoadingSheetItemResponse(

    Long id,
    Long productId,
    String productName,
    String productSku,
    
    Integer quantityLoaded,
    String unitType


) {}