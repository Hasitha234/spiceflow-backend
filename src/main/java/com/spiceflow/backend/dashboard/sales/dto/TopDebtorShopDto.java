package com.spiceflow.backend.dashboard.sales.dto;

import java.math.BigDecimal;

public record TopDebtorShopDto(
    Long shopId,
    String shopName,
    String ownerName,
    String phone,
    String area,
    BigDecimal outstandingLoan
) {}
