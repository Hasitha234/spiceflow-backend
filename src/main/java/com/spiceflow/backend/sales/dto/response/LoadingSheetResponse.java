package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record LoadingSheetResponse(

    
    Long id,
    
    Long repOrderId,
    String repOrderNumber,
    Long repId,
    String repName,
    
    Long driverId,
    String driverName,
    String driverVehicleNo,
    Boolean hasActiveDelivery,
    Long activeDeliveryId,
    
    LocalDate loadingDate,
    String status,
    
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    
    List<LoadingSheetItemResponse> items,
    List<LoadingSheetReturnResponse> returns


) {}