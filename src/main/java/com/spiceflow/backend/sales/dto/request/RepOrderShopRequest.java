package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Builder
public record RepOrderShopRequest(

    
    @NotNull(message = "Shop ID is required")
    Long shopId,
    
    @Valid
    @NotNull
    List<RepOrderItemRequest> items,
    
    @Valid
    List<ShopReturnRequest> returns



) {}