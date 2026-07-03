package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Builder
public record CreateDeliveryRequest(

    
    @NotNull(message = "Loading sheet ID is required")
    Long loadingSheetId,
    
    @NotNull(message = "Delivery date is required")
    LocalDate deliveryDate



) {}