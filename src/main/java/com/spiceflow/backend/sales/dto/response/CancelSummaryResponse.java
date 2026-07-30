package com.spiceflow.backend.sales.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record CancelSummaryResponse(
    Long id,
    Long tenantId,
    Long repId,
    String repName,
    Long driverId,
    String driverName,
    LocalDate summaryDate,
    String summaryNumber,
    BigDecimal finalEstimateValue,
    String status,
    List<CancelSummaryItemResponse> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String createdBy,
    String lastModifiedBy
) {}
