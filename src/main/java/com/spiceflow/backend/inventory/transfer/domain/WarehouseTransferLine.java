package com.spiceflow.backend.inventory.transfer.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jspecify.annotations.Nullable;

public class WarehouseTransferLine {

    private @Nullable Long id;
    private Long productId;
    private BigDecimal requestedQty;
    private BigDecimal shippedQty;
    private BigDecimal receivedQty;
    private BigDecimal damagedQty;
    private String lotNumber;
    private BigDecimal unitCost;
    private BigDecimal lineTotal;

    protected WarehouseTransferLine() {
        this.productId = 0L;
        this.requestedQty = BigDecimal.ZERO;
        this.shippedQty = BigDecimal.ZERO;
        this.receivedQty = BigDecimal.ZERO;
        this.damagedQty = BigDecimal.ZERO;
        this.lotNumber = "";
        this.unitCost = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
    }

    public WarehouseTransferLine(Long productId, BigDecimal requestedQty, BigDecimal shippedQty,
                                 BigDecimal receivedQty, BigDecimal damagedQty, String lotNumber,
                                 BigDecimal unitCost) {
        this(null, productId, requestedQty, shippedQty, receivedQty, damagedQty, lotNumber, unitCost);
    }

    public WarehouseTransferLine(@Nullable Long id, Long productId, BigDecimal requestedQty,
                                 BigDecimal shippedQty, BigDecimal receivedQty, BigDecimal damagedQty,
                                 String lotNumber, BigDecimal unitCost) {
        this.id = id;
        this.productId = productId;
        this.requestedQty = requestedQty;
        this.shippedQty = shippedQty;
        this.receivedQty = receivedQty;
        this.damagedQty = damagedQty;
        this.lotNumber = lotNumber != null ? lotNumber : "";
        this.unitCost = unitCost;
        this.lineTotal = shippedQty.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
    }

    public @Nullable Long getId() { return id; }
    public Long getProductId() { return productId; }
    public BigDecimal getRequestedQty() { return requestedQty; }
    public BigDecimal getShippedQty() { return shippedQty; }
    public BigDecimal getReceivedQty() { return receivedQty; }
    public BigDecimal getDamagedQty() { return damagedQty; }
    public String getLotNumber() { return lotNumber; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
