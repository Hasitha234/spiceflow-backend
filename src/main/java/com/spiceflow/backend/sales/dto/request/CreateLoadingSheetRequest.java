package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class CreateLoadingSheetRequest {
    
    @NotNull(message = "Rep order ID is required")
    private Long repOrderId;
    
    @NotNull(message = "Driver ID is required")
    private Long driverId;
    
    @NotNull(message = "Loading date is required")
    private LocalDate loadingDate;
}

