package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.math.BigDecimal;

@Builder
public record RepOrderShopRequest(

    
    @NotNull(message = "Shop ID is required")
    Long shopId,

    BigDecimal discountAmount,
    
    BigDecimal skuDiscountAmount,

    Long returnWarehouseId,
    
    @Valid
    @NotNull
    List<RepOrderItemRequest> items,
    
    @Valid
    List<ShopReturnRequest> returns



) {}