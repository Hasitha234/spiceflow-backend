package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class DeliveryPaymentResponse {
    
    private Long id;
    private String paymentMethod;
    private BigDecimal amount;
    
    private String chequeNo;
    private String chequeBankName;
    private LocalDate chequeDate;
    
    private OffsetDateTime createdAt;
}
