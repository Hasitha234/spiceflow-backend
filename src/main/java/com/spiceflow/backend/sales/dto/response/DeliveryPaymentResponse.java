package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Builder
public record DeliveryPaymentResponse(

    
    Long id,
    String paymentMethod,
    BigDecimal amount,
    
    String chequeNo,
    String chequeBankName,
    LocalDate chequeDate,
    
    OffsetDateTime createdAt


) {}