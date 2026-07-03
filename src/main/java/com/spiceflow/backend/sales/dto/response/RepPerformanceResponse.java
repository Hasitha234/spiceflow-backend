package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RepPerformanceResponse {
    private Long repId;
    private String repName;
    private Integer totalOrders;
    private BigDecimal totalSalesValue;
    private BigDecimal totalCollectedAmount;
    private BigDecimal performanceScore; // Optional metric based on target vs actual
}
