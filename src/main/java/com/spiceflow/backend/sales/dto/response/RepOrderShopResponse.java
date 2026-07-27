package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record RepOrderShopResponse(

    
    Long id,
    Long shopId,
    String shopName,
    String outletId,
    
    BigDecimal grossOrderAmount,
    BigDecimal discountAmount,
    BigDecimal skuDiscountAmount,
    BigDecimal returnsValue,
    BigDecimal netAmount,
    
    Long returnWarehouseId,
    String returnWarehouseName,
    
    OffsetDateTime createdAt,
    
    List<RepOrderItemResponse> items,
    List<ShopReturnResponse> returns


) {}