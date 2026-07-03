package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ShopOutstandingResponse(

    Long shopId,
    String shopName,
    @org.jspecify.annotations.Nullable
    String route,
    BigDecimal outstandingAmount


) {}