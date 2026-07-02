package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateDeliveryRequest {
    
    @NotNull(message = "Loading sheet ID is required")
    private Long loadingSheetId;
    
    @NotNull(message = "Delivery date is required")
    private LocalDate deliveryDate;
}
