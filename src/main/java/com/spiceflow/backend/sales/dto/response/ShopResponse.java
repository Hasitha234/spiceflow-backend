package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record ShopResponse(
    Long id,
    String name,
    String outletId,
    String phone,
    String address,
    String area,
    String route,
    Long assignedRepId,
    String assignedRepName,
    BigDecimal outstandingLoan,
    BigDecimal latitude,
    BigDecimal longitude,
    Boolean isActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}