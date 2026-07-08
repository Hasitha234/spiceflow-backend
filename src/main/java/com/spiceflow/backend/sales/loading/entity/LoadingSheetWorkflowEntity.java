package com.spiceflow.backend.sales.loading.entity;

import com.spiceflow.backend.sales.loading.domain.LoadingSheetState;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "loading_sheets")
public class LoadingSheetWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "sheet_number", unique = true)
    private @Nullable String sheetNumber;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "rep_order_id", nullable = false)
    private Long repOrderId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "loading_date", nullable = false)
    private LocalDate loadingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoadingSheetState status;

    @Column(name = "created_by")
    private @Nullable String createdBy;

    @Column(name = "confirmed_by")
    private @Nullable String confirmedBy;

    @Column(name = "cancelled_by")
    private @Nullable String cancelledBy;

    @Version
    private @Nullable Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "confirmed_at")
    private @Nullable Instant confirmedAt;

    @Column(name = "cancelled_at")
    private @Nullable Instant cancelledAt;

    @OneToMany(
        mappedBy = "loadingSheet",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<LoadingSheetItemWorkflowEntity> items = new ArrayList<>();

    @OneToMany(
        mappedBy = "loadingSheet",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<LoadingSheetReturnWorkflowEntity> returns = new ArrayList<>();

    public LoadingSheetWorkflowEntity() {
        this.tenantId = 0L;
        this.repOrderId = 0L;
        this.driverId = 0L;
        this.loadingDate = LocalDate.now(java.time.ZoneId.systemDefault());
        this.status = LoadingSheetState.DRAFT;
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public @Nullable String getSheetNumber() { return sheetNumber; }
    public void setSheetNumber(@Nullable String sheetNumber) { this.sheetNumber = sheetNumber; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getRepOrderId() { return repOrderId; }
    public void setRepOrderId(Long repOrderId) { this.repOrderId = repOrderId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public LocalDate getLoadingDate() { return loadingDate; }
    public void setLoadingDate(LocalDate loadingDate) { this.loadingDate = loadingDate; }

    public LoadingSheetState getStatus() { return status; }
    public void setStatus(LoadingSheetState status) { this.status = status; }

    public @Nullable String getCreatedBy() { return createdBy; }
    public void setCreatedBy(@Nullable String createdBy) { this.createdBy = createdBy; }

    public @Nullable String getConfirmedBy() { return confirmedBy; }
    public void setConfirmedBy(@Nullable String confirmedBy) { this.confirmedBy = confirmedBy; }

    public @Nullable String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(@Nullable String cancelledBy) { this.cancelledBy = cancelledBy; }

    public @Nullable Long getVersion() { return version; }
    public void setVersion(@Nullable Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public @Nullable Instant getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(@Nullable Instant confirmedAt) { this.confirmedAt = confirmedAt; }

    public @Nullable Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(@Nullable Instant cancelledAt) { this.cancelledAt = cancelledAt; }

    public List<LoadingSheetItemWorkflowEntity> getItems() { return items; }
    public void setItems(List<LoadingSheetItemWorkflowEntity> items) { this.items = items; }

    public List<LoadingSheetReturnWorkflowEntity> getReturns() { return returns; }
    public void setReturns(List<LoadingSheetReturnWorkflowEntity> returns) { this.returns = returns; }
}
