package com.spiceflow.backend.sales.order.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Immutable shop order record within a rep route order.
 * Rule 15 compliant: defensive copying of line items and returns; immutable state.
 */
public record RepOrderShop(
        @Nullable Long id,
        Long shopId,
        @Nullable BigDecimal grossOrderAmount,
        @Nullable BigDecimal returnsValue,
        @Nullable BigDecimal netAmount,
        List<RepOrderItem> items,
        List<ShopReturnItem> returns
) {
    public RepOrderShop {
        Objects.requireNonNull(shopId, "shopId cannot be null");
        items = items != null ? List.copyOf(items) : Collections.emptyList();
        returns = returns != null ? List.copyOf(returns) : Collections.emptyList();

        BigDecimal computedGross = items.stream()
                .map(item -> item.netAmount() != null ? item.netAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal computedReturns = returns.stream()
                .map(ShopReturnItem::creditValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        grossOrderAmount = grossOrderAmount != null ? grossOrderAmount : computedGross;
        returnsValue = returnsValue != null ? returnsValue : computedReturns;
        netAmount = netAmount != null ? netAmount : grossOrderAmount.subtract(returnsValue);
    }

    public RepOrderShop(Long shopId, List<RepOrderItem> items, List<ShopReturnItem> returns) {
        this(null, shopId, null, null, null, items, returns);
    }
}
