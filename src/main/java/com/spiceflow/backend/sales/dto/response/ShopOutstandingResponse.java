package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ShopOutstandingResponse {
    private Long shopId;
    private String shopName;
    @org.jspecify.annotations.Nullable
    private String route;
    private BigDecimal outstandingAmount;
}
