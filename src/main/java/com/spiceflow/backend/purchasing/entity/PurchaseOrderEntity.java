package com.spiceflow.backend.purchasing.entity;

import com.spiceflow.backend.purchasing.domain.PurchaseOrderState;
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
@Table(name = "purchase_orders")
public class PurchaseOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "correlation_id", nullable = false, unique = true)
    private String correlationId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PurchaseOrderState status;

    @Version
    private @Nullable Long version;

    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
        mappedBy = "purchaseOrder",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PurchaseOrderLineEntity> lines = new ArrayList<>();

    public PurchaseOrderEntity() {
        this.correlationId = "";
        this.tenantId = 0L;
        this.supplierId = 0L;
        this.status = PurchaseOrderState.DRAFT;
        this.orderDate = Instant.EPOCH;
        this.totalAmount = BigDecimal.ZERO;
        this.createdBy = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public PurchaseOrderState getStatus() { return status; }
    public void setStatus(PurchaseOrderState status) { this.status = status; }

    public @Nullable Long getVersion() { return version; }
    public void setVersion(@Nullable Long version) { this.version = version; }

    public Instant getOrderDate() { return orderDate; }
    public void setOrderDate(Instant orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<PurchaseOrderLineEntity> getLines() { return lines; }
    public void setLines(List<PurchaseOrderLineEntity> lines) {
        this.lines.clear();
        if (lines != null) {
            for (PurchaseOrderLineEntity line : lines) {
                addLine(line);
            }
        }
    }

    public void addLine(PurchaseOrderLineEntity line) {
        this.lines.add(line);
        line.setPurchaseOrder(this);
    }
}
