package com.spiceflow.backend.inventory.transfer.entity;

import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
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
@Table(name = "warehouse_transfers")
public class WarehouseTransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "transfer_number", nullable = false, unique = true)
    private String transferNumber;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "from_warehouse_id", nullable = false)
    private Long fromWarehouseId;

    @Column(name = "to_warehouse_id", nullable = false)
    private Long toWarehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WarehouseTransferState status;

    @Column(name = "request_date", nullable = false)
    private Instant requestDate;

    @Column(name = "total_transfer_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalTransferValue;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "approved_by")
    private @Nullable String approvedBy;

    @Column(name = "shipped_by")
    private @Nullable String shippedBy;

    @Column(name = "received_by")
    private @Nullable String receivedBy;

    @Version
    private @Nullable Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "approved_at")
    private @Nullable Instant approvedAt;

    @Column(name = "shipped_at")
    private @Nullable Instant shippedAt;

    @Column(name = "received_at")
    private @Nullable Instant receivedAt;

    @OneToMany(
        mappedBy = "warehouseTransfer",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<WarehouseTransferLineEntity> lines = new ArrayList<>();

    public WarehouseTransferEntity() {
        this.transferNumber = "";
        this.tenantId = 0L;
        this.fromWarehouseId = 0L;
        this.toWarehouseId = 0L;
        this.status = WarehouseTransferState.DRAFT;
        this.requestDate = Instant.EPOCH;
        this.totalTransferValue = BigDecimal.ZERO;
        this.createdBy = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public String getTransferNumber() { return transferNumber; }
    public void setTransferNumber(String transferNumber) { this.transferNumber = transferNumber; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(Long fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }

    public Long getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(Long toWarehouseId) { this.toWarehouseId = toWarehouseId; }

    public WarehouseTransferState getStatus() { return status; }
    public void setStatus(WarehouseTransferState status) { this.status = status; }

    public Instant getRequestDate() { return requestDate; }
    public void setRequestDate(Instant requestDate) { this.requestDate = requestDate; }

    public BigDecimal getTotalTransferValue() { return totalTransferValue; }
    public void setTotalTransferValue(BigDecimal totalTransferValue) { this.totalTransferValue = totalTransferValue; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public @Nullable String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(@Nullable String approvedBy) { this.approvedBy = approvedBy; }

    public @Nullable String getShippedBy() { return shippedBy; }
    public void setShippedBy(@Nullable String shippedBy) { this.shippedBy = shippedBy; }

    public @Nullable String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(@Nullable String receivedBy) { this.receivedBy = receivedBy; }

    public @Nullable Long getVersion() { return version; }
    public void setVersion(@Nullable Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public @Nullable Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(@Nullable Instant approvedAt) { this.approvedAt = approvedAt; }

    public @Nullable Instant getShippedAt() { return shippedAt; }
    public void setShippedAt(@Nullable Instant shippedAt) { this.shippedAt = shippedAt; }

    public @Nullable Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(@Nullable Instant receivedAt) { this.receivedAt = receivedAt; }

    public List<WarehouseTransferLineEntity> getLines() { return lines; }
    public void setLines(List<WarehouseTransferLineEntity> lines) {
        this.lines.clear();
        if (lines != null) {
            for (WarehouseTransferLineEntity line : lines) {
                addLine(line);
            }
        }
    }

    public void addLine(WarehouseTransferLineEntity line) {
        this.lines.add(line);
        line.setWarehouseTransfer(this);
    }
}
