package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockStatusResponse {
    private Long productId;
    private String productName;
    private String productCode;
    private Integer mainStoreQuantity;
    private Integer otherStoresQuantity;
    private Integer totalQuantity;
}
