package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Builder
public record CreateLoadingSheetRequest(

    
    @NotNull(message = "Rep order ID is required")
    Long repOrderId,
    
    @NotNull(message = "Driver ID is required")
    Long driverId,
    
    @NotNull(message = "Loading date is required")
    LocalDate loadingDate



) {}