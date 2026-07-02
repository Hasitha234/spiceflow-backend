package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class RecordShopDeliveryRequest {
    
    @Valid
    @NotNull
    private List<DeliveryShopItemRequest> items;
    
    @Valid
    private List<DeliveryShopReturnRequest> returns;
    
    @Valid
    private List<DeliveryPaymentRequest> payments;
}
