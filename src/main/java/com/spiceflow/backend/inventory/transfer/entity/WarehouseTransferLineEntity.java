package com.spiceflow.backend.inventory.transfer.entity;


import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "warehouse_transfer_lines")
public class WarehouseTransferLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "warehouse_transfer_id", nullable = false)
    private @Nullable WarehouseTransferEntity warehouseTransfer;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "requested_qty", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedQty;

    @Column(name = "shipped_qty", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippedQty;

    @Column(name = "received_qty", nullable = false, precision = 19, scale = 2)
    private BigDecimal receivedQty;

    @Column(name = "damaged_qty", nullable = false, precision = 19, scale = 2)
    private BigDecimal damagedQty;

    @Column(name = "lot_number", nullable = false)
    private String lotNumber;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    public WarehouseTransferLineEntity() {
        this.productId = 0L;
        this.requestedQty = BigDecimal.ZERO;
        this.shippedQty = BigDecimal.ZERO;
        this.receivedQty = BigDecimal.ZERO;
        this.damagedQty = BigDecimal.ZERO;
        this.lotNumber = "";
        this.unitCost = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public @Nullable WarehouseTransferEntity getWarehouseTransfer() { return warehouseTransfer; }
    public void setWarehouseTransfer(@Nullable WarehouseTransferEntity warehouseTransfer) { this.warehouseTransfer = warehouseTransfer; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getRequestedQty() { return requestedQty; }
    public void setRequestedQty(BigDecimal requestedQty) { this.requestedQty = requestedQty; }

    public BigDecimal getShippedQty() { return shippedQty; }
    public void setShippedQty(BigDecimal shippedQty) { this.shippedQty = shippedQty; }

    public BigDecimal getReceivedQty() { return receivedQty; }
    public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }

    public BigDecimal getDamagedQty() { return damagedQty; }
    public void setDamagedQty(BigDecimal damagedQty) { this.damagedQty = damagedQty; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
