package com.spiceflow.backend.sales.delivery.domain;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Immutable record representing a single shop stop within a Delivery.
 * Encapsulates delivered items, returned items, payments, and financial totals.
 * ADR-013: Self-validating, NullAway-safe, no setters.
 */
public record DeliveryShopRecord(
        @Nullable Long id,
        Long shopId,
        BigDecimal grossBillAmount,
        BigDecimal totalDiscount,
        BigDecimal returnsDeducted,
        BigDecimal netPayable,
        BigDecimal paidAmount,
        BigDecimal creditAmount,
        List<DeliveryShopItemRecord> items,
        List<DeliveryReturnItemRecord> returns,
        List<DeliveryPaymentRecord> payments
) {
    public DeliveryShopRecord {
        if (shopId == null) throw new IllegalArgumentException("shopId cannot be null");
        if (grossBillAmount == null) throw new IllegalArgumentException("grossBillAmount cannot be null");
        if (totalDiscount == null) throw new IllegalArgumentException("totalDiscount cannot be null");
        if (returnsDeducted == null) throw new IllegalArgumentException("returnsDeducted cannot be null");
        if (netPayable == null) throw new IllegalArgumentException("netPayable cannot be null");
        if (paidAmount == null) throw new IllegalArgumentException("paidAmount cannot be null");
        if (creditAmount == null) throw new IllegalArgumentException("creditAmount cannot be null");
        // Defensive copy of lists (NullAway-safe)
        items = items != null ? List.copyOf(items) : List.of();
        returns = returns != null ? List.copyOf(returns) : List.of();
        payments = payments != null ? List.copyOf(payments) : List.of();
    }

    /** Convenience constructor without a persisted id. */
    public DeliveryShopRecord(Long shopId, BigDecimal grossBillAmount, BigDecimal totalDiscount,
                               BigDecimal returnsDeducted, BigDecimal netPayable, BigDecimal paidAmount,
                               BigDecimal creditAmount, List<DeliveryShopItemRecord> items,
                               List<DeliveryReturnItemRecord> returns, List<DeliveryPaymentRecord> payments) {
        this(null, shopId, grossBillAmount, totalDiscount, returnsDeducted, netPayable, paidAmount, creditAmount,
                items, returns, payments);
    }
}
