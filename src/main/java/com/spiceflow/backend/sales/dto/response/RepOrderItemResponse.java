package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RepOrderItemResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    
    private Integer quantity;
    private String unitType;
    private BigDecimal rate;
    
    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    
    private Boolean isFreeItem;
    private Integer boxesNeeded;
}
