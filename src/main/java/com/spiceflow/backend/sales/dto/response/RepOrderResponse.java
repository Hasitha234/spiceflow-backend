package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class RepOrderResponse {
    
    private Long id;
    private Long repId;
    private String repName;
    private LocalDate orderDate;
    private String routeArea;
    
    private BigDecimal totalGrossAmount;
    private BigDecimal totalReturnsValue;
    private BigDecimal netAmount;
    
    private String loadingStatus;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    private List<RepOrderShopResponse> shops;
}
