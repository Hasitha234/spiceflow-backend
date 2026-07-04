package com.spiceflow.backend.purchasing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jspecify.annotations.Nullable;

public class PurchaseOrderLine {

    private @Nullable Long id;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    protected PurchaseOrderLine() {
        this.productId = 0L;
        this.quantity = BigDecimal.ZERO;
        this.unitPrice = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
    }

    public PurchaseOrderLine(Long productId, BigDecimal quantity, BigDecimal unitPrice) {
        this(null, productId, quantity, unitPrice);
    }

    public PurchaseOrderLine(@Nullable Long id, Long productId, BigDecimal quantity, BigDecimal unitPrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    public @Nullable Long getId() { return id; }
    public Long getProductId() { return productId; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
