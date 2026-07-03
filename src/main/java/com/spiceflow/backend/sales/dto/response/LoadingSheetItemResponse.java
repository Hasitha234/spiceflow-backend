package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadingSheetItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    
    private Integer quantityLoaded;
    private String unitType;
}
