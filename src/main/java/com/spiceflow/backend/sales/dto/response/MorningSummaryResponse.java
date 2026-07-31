package com.spiceflow.backend.sales.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MorningSummaryResponse {
    private Long id;
    private String summaryNumber;
    private LocalDate summaryDate;
    private BigDecimal finalEstimateValue;
    private String status;
    private Long repId;
    private String repName;
    private Long driverId;
    private String driverName;
    
    @org.jspecify.annotations.Nullable
    private Long deductedWarehouseId;
    
    @org.jspecify.annotations.Nullable
    private String deductedWarehouseName;

    @org.jspecify.annotations.Nullable
    private Long returnWarehouseId;
    
    @org.jspecify.annotations.Nullable
    private String returnWarehouseName;
    
    private List<MorningSummaryItemResponse> items;

    @Data
    @Builder
    public static class MorningSummaryItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal estimateValue;
        private Integer expectedReturnAmount;
        private BigDecimal expectedReturnPrice;
    }
}
