package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class DeliveryPaymentRequest {
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal amount;
    
    private String chequeNo;
    private String chequeBankName;
    private LocalDate chequeDate;
}

