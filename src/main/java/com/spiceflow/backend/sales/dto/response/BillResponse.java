package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Builder
public record BillResponse(
        Long id,
        String billNumber,
        LocalDate billDate,
        
        Long repId,
        String repName,
        
        @org.jspecify.annotations.Nullable Long driverId,
        @org.jspecify.annotations.Nullable String driverName,
        
        Long shopId,
        String shopName,
        
        BigDecimal netTotal,
        BigDecimal reverseGrts,
        BigDecimal freeItemsValue,
        BigDecimal discount,
        BigDecimal skuDiscount,
        BigDecimal finalTotal,
        
        String status,
        BigDecimal cashCollected,
        BigDecimal checkCollected,
        BigDecimal loanAmount,
        @org.jspecify.annotations.Nullable LocalDate loanDueDate,
        String loanStatus,
        
        OffsetDateTime createdAt
) {}
