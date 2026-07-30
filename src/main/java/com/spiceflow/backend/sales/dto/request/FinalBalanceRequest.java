package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@SuppressWarnings("NullAway.Init")
public class FinalBalanceRequest {
    @NotNull(message = "Rep ID is required")
    private Long repId;
    
    @org.jspecify.annotations.Nullable
    private Long driverId;
    
    @NotNull(message = "Date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate balanceDate;
    
    @org.jspecify.annotations.Nullable
    private String remarks;
}
