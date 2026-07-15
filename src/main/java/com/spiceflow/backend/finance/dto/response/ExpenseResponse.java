package com.spiceflow.backend.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
    Long id,
    BigDecimal amount,
    String category,
    String description,
    LocalDate date
) {}
