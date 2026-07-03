package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class RepOrderShopResponse {
    
    private Long id;
    private Long shopId;
    private String shopName;
    
    private BigDecimal grossOrderAmount;
    private BigDecimal returnsValue;
    private BigDecimal netAmount;
    
    private OffsetDateTime createdAt;
    
    private List<RepOrderItemResponse> items;
    private List<ShopReturnResponse> returns;
}
