package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class ShopReturnResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    
    private Integer quantity;
    private String unitType;
    private BigDecimal creditValue;
    
    private String returnType;
    private String status;
    
    private OffsetDateTime createdAt;
}
