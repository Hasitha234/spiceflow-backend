package com.spiceflow.backend.sales.delivery.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * Immutable record representing a payment collected at point of delivery.
 * ADR-013: Self-validating, NullAway-safe, no setters.
 * paymentMethod: CASH | CHEQUE | BANK_TRANSFER
 */
public record DeliveryPaymentRecord(
        @Nullable Long id,
        String paymentMethod,
        BigDecimal amount,
        @Nullable String chequeNo,
        @Nullable String chequeBankName,
        @Nullable LocalDate chequeDate
) {
    public DeliveryPaymentRecord {
        if (paymentMethod == null || paymentMethod.isBlank()) throw new IllegalArgumentException("paymentMethod cannot be blank");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount must be positive");
    }

    /** Convenience constructor without a persisted id. */
    public DeliveryPaymentRecord(String paymentMethod, BigDecimal amount,
                                  @Nullable String chequeNo, @Nullable String chequeBankName,
                                  @Nullable LocalDate chequeDate) {
        this(null, paymentMethod, amount, chequeNo, chequeBankName, chequeDate);
    }
}
