package com.spiceflow.backend.dashboard.finance.dto;

import java.math.BigDecimal;

public record ReceivableAgingBucketDto(
    String bucketLabel,
    long shopCount,
    BigDecimal totalAmount
) {}
