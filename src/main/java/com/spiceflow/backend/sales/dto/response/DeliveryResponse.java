package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class DeliveryResponse {
    
    private Long id;
    private Long loadingSheetId;
    
    private LocalDate deliveryDate;
    private String status;
    
    private BigDecimal totalSalesValue;
    private BigDecimal totalReturnsValue;
    private BigDecimal totalCollectedAmount;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    private List<DeliveryShopResponse> shops;
}
