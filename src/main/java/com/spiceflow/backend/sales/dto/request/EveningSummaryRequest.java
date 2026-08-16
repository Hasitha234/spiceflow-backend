package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record EveningSummaryRequest(
    @NotNull(message = "Rep ID is required")
    Long repId,

    @NotNull(message = "Driver ID is required")
    Long driverId,

    @NotNull(message = "Summary date is required")
    LocalDate summaryDate,

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    List<EveningSummaryItemRequest> items
) {}
