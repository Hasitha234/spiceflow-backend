package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class RepOrderShopRequest {
    
    @NotNull(message = "Shop ID is required")
    private Long shopId;
    
    @Valid
    @NotNull
    private List<RepOrderItemRequest> items;
    
    @Valid
    private List<ShopReturnRequest> returns;
}

