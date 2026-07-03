package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class LoadingSheetResponse {
    
    private Long id;
    
    private Long repOrderId;
    private Long repId;
    private String repName;
    
    private Long driverId;
    private String driverName;
    
    private LocalDate loadingDate;
    private String status;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    private List<LoadingSheetItemResponse> items;
    private List<LoadingSheetReturnResponse> returns;
}
