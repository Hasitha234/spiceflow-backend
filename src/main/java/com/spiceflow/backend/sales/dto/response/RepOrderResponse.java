package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record RepOrderResponse(

    
    Long id,
    Long repId,
    String repName,
    LocalDate orderDate,
    String routeArea,
    
    BigDecimal totalGrossAmount,
    BigDecimal totalReturnsValue,
    BigDecimal netAmount,
    
    String loadingStatus,
    
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    
    List<RepOrderShopResponse> shops


) {}