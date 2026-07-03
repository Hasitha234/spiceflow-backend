package com.spiceflow.backend.purchase.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@SuppressWarnings("NullAway.Init")
public class PurchaseLineItemResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    
    private Integer noOfBoxes;
    private Integer soldQuantity;
    private String unitType;
    private BigDecimal rate;
    private BigDecimal amount;
}

