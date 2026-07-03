package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record DeliveryPaymentRequest(

    
    @NotBlank(message = "Payment method is required")
    String paymentMethod,
    
    @NotNull
    @PositiveOrZero
    BigDecimal amount,
    
    String chequeNo,
    String chequeBankName,
    LocalDate chequeDate



) {}