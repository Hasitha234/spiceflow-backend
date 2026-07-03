package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record DeliveryResponse(

    
    Long id,
    Long loadingSheetId,
    
    LocalDate deliveryDate,
    String status,
    
    BigDecimal totalSalesValue,
    BigDecimal totalReturnsValue,
    BigDecimal totalCollectedAmount,
    
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    
    List<DeliveryShopResponse> shops


) {}