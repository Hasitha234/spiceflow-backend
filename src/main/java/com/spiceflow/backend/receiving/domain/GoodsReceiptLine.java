package com.spiceflow.backend.receiving.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public class GoodsReceiptLine {

    private @Nullable Long id;
    private Long productId;
    private BigDecimal expectedQty;
    private BigDecimal receivedQty;
    private BigDecimal acceptedQty;
    private BigDecimal damagedQty;
    private String lotNumber;
    private @Nullable LocalDate expirationDate;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    protected GoodsReceiptLine() {
        this.productId = 0L;
        this.expectedQty = BigDecimal.ZERO;
        this.receivedQty = BigDecimal.ZERO;
        this.acceptedQty = BigDecimal.ZERO;
        this.damagedQty = BigDecimal.ZERO;
        this.lotNumber = "";
        this.expirationDate = null;
        this.unitPrice = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
    }

    public GoodsReceiptLine(Long productId, BigDecimal expectedQty, BigDecimal receivedQty,
                            BigDecimal acceptedQty, BigDecimal damagedQty, String lotNumber,
                            @Nullable LocalDate expirationDate, BigDecimal unitPrice) {
        this(null, productId, expectedQty, receivedQty, acceptedQty, damagedQty, lotNumber, expirationDate, unitPrice);
    }

    public GoodsReceiptLine(@Nullable Long id, Long productId, BigDecimal expectedQty,
                            BigDecimal receivedQty, BigDecimal acceptedQty, BigDecimal damagedQty,
                            String lotNumber, @Nullable LocalDate expirationDate, BigDecimal unitPrice) {
        this.id = id;
        this.productId = productId;
        this.expectedQty = expectedQty;
        this.receivedQty = receivedQty;
        this.acceptedQty = acceptedQty;
        this.damagedQty = damagedQty;
        this.lotNumber = lotNumber != null ? lotNumber : "";
        this.expirationDate = expirationDate;
        this.unitPrice = unitPrice;
        this.lineTotal = acceptedQty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    public @Nullable Long getId() { return id; }
    public Long getProductId() { return productId; }
    public BigDecimal getExpectedQty() { return expectedQty; }
    public BigDecimal getReceivedQty() { return receivedQty; }
    public BigDecimal getAcceptedQty() { return acceptedQty; }
    public BigDecimal getDamagedQty() { return damagedQty; }
    public String getLotNumber() { return lotNumber; }
    public @Nullable LocalDate getExpirationDate() { return expirationDate; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
