package com.spiceflow.backend.sales.delivery.entity;

import com.spiceflow.backend.sales.delivery.domain.DeliveryState;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * JPA entity mapping the {@code deliveries} table for the workflow execution layer.
 * This entity is exclusively used by {@code DeliveryPersistenceAdapter} and must
 * never be exposed to or manipulated by services directly.
 */
@Entity
@Table(name = "deliveries")
public class DeliveryWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "delivery_number")
    private @Nullable String deliveryNumber;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "loading_sheet_id", nullable = false)
    private Long loadingSheetId;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryState status;

    @Column(name = "total_sales_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSalesValue;

    @Column(name = "total_returns_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalReturnsValue;

    @Column(name = "total_collected_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCollectedAmount;

    @Column(name = "created_by")
    private @Nullable String createdBy;

    @Column(name = "dispatched_by")
    private @Nullable String dispatchedBy;

    @Column(name = "completed_by")
    private @Nullable String completedBy;

    @Column(name = "cancelled_by")
    private @Nullable String cancelledBy;

    @Version
    private @Nullable Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "dispatched_at")
    private @Nullable Instant dispatchedAt;

    @Column(name = "completed_at")
    private @Nullable Instant completedAt;

    @Column(name = "cancelled_at")
    private @Nullable Instant cancelledAt;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryShopWorkflowEntity> shops = new ArrayList<>();

    public DeliveryWorkflowEntity() {
        this.tenantId = 0L;
        this.loadingSheetId = 0L;
        this.deliveryDate = LocalDate.now(ZoneOffset.UTC);
        this.status = DeliveryState.IN_PROGRESS;
        this.totalSalesValue = BigDecimal.ZERO;
        this.totalReturnsValue = BigDecimal.ZERO;
        this.totalCollectedAmount = BigDecimal.ZERO;
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public @Nullable String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(@Nullable String deliveryNumber) { this.deliveryNumber = deliveryNumber; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getLoadingSheetId() { return loadingSheetId; }
    public void setLoadingSheetId(Long loadingSheetId) { this.loadingSheetId = loadingSheetId; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public DeliveryState getStatus() { return status; }
    public void setStatus(DeliveryState status) { this.status = status; }

    public BigDecimal getTotalSalesValue() { return totalSalesValue; }
    public void setTotalSalesValue(BigDecimal totalSalesValue) { this.totalSalesValue = totalSalesValue; }

    public BigDecimal getTotalReturnsValue() { return totalReturnsValue; }
    public void setTotalReturnsValue(BigDecimal totalReturnsValue) { this.totalReturnsValue = totalReturnsValue; }

    public BigDecimal getTotalCollectedAmount() { return totalCollectedAmount; }
    public void setTotalCollectedAmount(BigDecimal totalCollectedAmount) { this.totalCollectedAmount = totalCollectedAmount; }

    public @Nullable String getCreatedBy() { return createdBy; }
    public void setCreatedBy(@Nullable String createdBy) { this.createdBy = createdBy; }

    public @Nullable String getDispatchedBy() { return dispatchedBy; }
    public void setDispatchedBy(@Nullable String dispatchedBy) { this.dispatchedBy = dispatchedBy; }

    public @Nullable String getCompletedBy() { return completedBy; }
    public void setCompletedBy(@Nullable String completedBy) { this.completedBy = completedBy; }

    public @Nullable String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(@Nullable String cancelledBy) { this.cancelledBy = cancelledBy; }

    public @Nullable Long getVersion() { return version; }
    public void setVersion(@Nullable Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public @Nullable Instant getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(@Nullable Instant dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public @Nullable Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(@Nullable Instant completedAt) { this.completedAt = completedAt; }

    public @Nullable Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(@Nullable Instant cancelledAt) { this.cancelledAt = cancelledAt; }

    public List<DeliveryShopWorkflowEntity> getShops() { return shops; }
    public void setShops(List<DeliveryShopWorkflowEntity> shops) { this.shops = shops; }
}
