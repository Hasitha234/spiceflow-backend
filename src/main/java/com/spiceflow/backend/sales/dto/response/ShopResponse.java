package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class ShopResponse {
    private Long id;
    private String name;
    private String ownerName;
    private String phone;
    private String address;
    private String area;
    private String route;
    
    private Long assignedRepId;
    private String assignedRepName;
    
    private BigDecimal outstandingLoan;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
