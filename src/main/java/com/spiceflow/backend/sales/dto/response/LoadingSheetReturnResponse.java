package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadingSheetReturnResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    
    private Integer quantityReturned;
    private String unitType;
    private String returnType;
}
