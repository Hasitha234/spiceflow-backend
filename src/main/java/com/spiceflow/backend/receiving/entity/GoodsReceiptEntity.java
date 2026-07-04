package com.spiceflow.backend.receiving.entity;

import com.spiceflow.backend.receiving.domain.GoodsReceiptState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "goods_receipts")
public class GoodsReceiptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "receipt_number", nullable = false, unique = true)
    private String receiptNumber;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "purchase_order_id")
    private @Nullable Long purchaseOrderId;

    @Column(name = "po_number", nullable = false)
    private String poNumber;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GoodsReceiptState status;

    @Column(name = "receipt_date", nullable = false)
    private Instant receiptDate;

    @Column(name = "total_accepted_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAcceptedValue;

    @Column(name = "total_damaged_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDamagedValue;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "verified_by")
    private @Nullable String verifiedBy;

    @Column(name = "posted_by")
    private @Nullable String postedBy;

    @Version
    private @Nullable Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "verified_at")
    private @Nullable Instant verifiedAt;

    @Column(name = "posted_at")
    private @Nullable Instant postedAt;

    @OneToMany(
        mappedBy = "goodsReceipt",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<GoodsReceiptLineEntity> lines = new ArrayList<>();

    public GoodsReceiptEntity() {
        this.receiptNumber = "";
        this.tenantId = 0L;
        this.purchaseOrderId = null;
        this.poNumber = "";
        this.supplierId = 0L;
        this.warehouseId = 0L;
        this.status = GoodsReceiptState.DRAFT;
        this.receiptDate = Instant.EPOCH;
        this.totalAcceptedValue = BigDecimal.ZERO;
        this.totalDamagedValue = BigDecimal.ZERO;
        this.createdBy = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public @Nullable Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(@Nullable Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public GoodsReceiptState getStatus() { return status; }
    public void setStatus(GoodsReceiptState status) { this.status = status; }

    public Instant getReceiptDate() { return receiptDate; }
    public void setReceiptDate(Instant receiptDate) { this.receiptDate = receiptDate; }

    public BigDecimal getTotalAcceptedValue() { return totalAcceptedValue; }
    public void setTotalAcceptedValue(BigDecimal totalAcceptedValue) { this.totalAcceptedValue = totalAcceptedValue; }

    public BigDecimal getTotalDamagedValue() { return totalDamagedValue; }
    public void setTotalDamagedValue(BigDecimal totalDamagedValue) { this.totalDamagedValue = totalDamagedValue; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public @Nullable String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(@Nullable String verifiedBy) { this.verifiedBy = verifiedBy; }

    public @Nullable String getPostedBy() { return postedBy; }
    public void setPostedBy(@Nullable String postedBy) { this.postedBy = postedBy; }

    public @Nullable Long getVersion() { return version; }
    public void setVersion(@Nullable Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public @Nullable Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(@Nullable Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public @Nullable Instant getPostedAt() { return postedAt; }
    public void setPostedAt(@Nullable Instant postedAt) { this.postedAt = postedAt; }

    public List<GoodsReceiptLineEntity> getLines() { return lines; }
    public void setLines(List<GoodsReceiptLineEntity> lines) {
        this.lines.clear();
        if (lines != null) {
            for (GoodsReceiptLineEntity line : lines) {
                addLine(line);
            }
        }
    }

    public void addLine(GoodsReceiptLineEntity line) {
        this.lines.add(line);
        line.setGoodsReceipt(this);
    }
}
