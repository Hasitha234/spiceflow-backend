package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;


@Builder
public record LoadingSheetReturnResponse(

    Long id,
    Long productId,
    String productName,
    String productSku,
    
    Integer quantityReturned,
    String unitType,
    String returnType


) {}