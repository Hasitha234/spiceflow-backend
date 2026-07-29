package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Builder
public record RecordShopDeliveryRequest(

    
    @Valid
    @NotNull
    List<DeliveryShopItemRequest> items,
    
    @Valid
    List<DeliveryShopReturnRequest> returns,
    
    @Valid
    List<DeliveryPaymentRequest> payments,
    
    @jakarta.validation.constraints.Min(0)
    java.math.BigDecimal discountAmount,
    
    @jakarta.validation.constraints.Min(0)
    java.math.BigDecimal skuDiscountAmount,
    
    @jakarta.validation.constraints.Min(0)
    java.math.BigDecimal reverseGrts,
    
    Double latitude,
    
    Double longitude,
    
    Double locationAccuracy,
    
    String notes

) {}