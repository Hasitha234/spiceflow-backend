package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class DeliveryShopReturnResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    
    private Integer quantityReturned;
    private String unitType;
    private BigDecimal creditValue;
    
    private String returnType;
    
    private OffsetDateTime createdAt;
}
