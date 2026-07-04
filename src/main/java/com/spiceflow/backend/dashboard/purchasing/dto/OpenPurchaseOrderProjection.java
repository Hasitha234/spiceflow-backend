package com.spiceflow.backend.dashboard.purchasing.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OpenPurchaseOrderProjection(
    String poNumber,
    Long supplierId,
    String supplierName,
    Instant orderDate,
    BigDecimal totalAmount,
    String status,
    long ageInDays
) {}
