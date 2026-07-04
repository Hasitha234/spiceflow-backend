package com.spiceflow.backend.dashboard.finance.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RecentFinancialTransactionDto(
    Long id,
    String transactionType,
    String referenceNumber,
    String partyName,
    BigDecimal amount,
    String paymentMethod,
    Instant timestamp,
    String status
) {}
