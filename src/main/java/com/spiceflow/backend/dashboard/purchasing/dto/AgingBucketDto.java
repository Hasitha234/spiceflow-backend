package com.spiceflow.backend.dashboard.purchasing.dto;

import java.math.BigDecimal;

public record AgingBucketDto(
    String bucketLabel,
    long orderCount,
    BigDecimal totalValue
) {}
