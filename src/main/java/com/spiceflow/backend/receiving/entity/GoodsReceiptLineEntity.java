package com.spiceflow.backend.receiving.entity;

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
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "goods_receipt_lines")
public class GoodsReceiptLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private @Nullable GoodsReceiptEntity goodsReceipt;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "expected_qty", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedQty;

    @Column(name = "received_qty", nullable = false, precision = 19, scale = 2)
    private BigDecimal receivedQty;

    @Column(name = "accepted_qty", nullable = false, precision = 19, scale = 2)
    private BigDecimal acceptedQty;

    @Column(name = "damaged_qty", nullable = false, precision = 19, scale = 2)
    private BigDecimal damagedQty;

    @Column(name = "lot_number", nullable = false)
    private String lotNumber;

    @Column(name = "expiration_date")
    private @Nullable LocalDate expirationDate;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    public GoodsReceiptLineEntity() {
        this.productId = 0L;
        this.expectedQty = BigDecimal.ZERO;
        this.receivedQty = BigDecimal.ZERO;
        this.acceptedQty = BigDecimal.ZERO;
        this.damagedQty = BigDecimal.ZERO;
        this.lotNumber = "";
        this.unitPrice = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public @Nullable GoodsReceiptEntity getGoodsReceipt() { return goodsReceipt; }
    public void setGoodsReceipt(@Nullable GoodsReceiptEntity goodsReceipt) { this.goodsReceipt = goodsReceipt; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getExpectedQty() { return expectedQty; }
    public void setExpectedQty(BigDecimal expectedQty) { this.expectedQty = expectedQty; }

    public BigDecimal getReceivedQty() { return receivedQty; }
    public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }

    public BigDecimal getAcceptedQty() { return acceptedQty; }
    public void setAcceptedQty(BigDecimal acceptedQty) { this.acceptedQty = acceptedQty; }

    public BigDecimal getDamagedQty() { return damagedQty; }
    public void setDamagedQty(BigDecimal damagedQty) { this.damagedQty = damagedQty; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public @Nullable LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(@Nullable LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
