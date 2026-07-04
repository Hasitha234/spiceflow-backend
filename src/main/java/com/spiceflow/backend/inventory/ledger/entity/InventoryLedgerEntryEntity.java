package com.spiceflow.backend.inventory.ledger.entity;

import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Immutable append-only double-entry stock ledger entity.
 * Strict Rule 15 & ADR-013: All columns updatable = false, no setters.
 */
@Entity
@Table(name = "inventory_ledger_entries")
public class InventoryLedgerEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @Column(name = "warehouse_id", nullable = false, updatable = false)
    private Long warehouseId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, updatable = false)
    private InventoryMovementType movementType;

    @Column(name = "quantity", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_value", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "reference_id", nullable = false, updatable = false, length = 50)
    private String referenceId;

    @Column(name = "lot_number", nullable = false, updatable = false, length = 100)
    private String lotNumber;

    @Column(name = "expiration_date", updatable = false)
    private @Nullable LocalDate expirationDate;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    @Column(name = "performed_by", nullable = false, updatable = false, length = 100)
    private String performedBy;

    protected InventoryLedgerEntryEntity() {
        this.tenantId = 0L;
        this.warehouseId = 0L;
        this.productId = 0L;
        this.movementType = InventoryMovementType.RECEIPT;
        this.quantity = BigDecimal.ZERO;
        this.unitCost = BigDecimal.ZERO;
        this.totalValue = BigDecimal.ZERO;
        this.referenceId = "";
        this.lotNumber = "";
        this.timestamp = Instant.EPOCH;
        this.performedBy = "";
    }

    public InventoryLedgerEntryEntity(Long tenantId, Long warehouseId, Long productId,
                                      InventoryMovementType movementType, BigDecimal quantity,
                                      BigDecimal unitCost, BigDecimal totalValue, String referenceId,
                                      String lotNumber, @Nullable LocalDate expirationDate,
                                      Instant timestamp, String performedBy) {
        this.tenantId = Objects.requireNonNull(tenantId);
        this.warehouseId = Objects.requireNonNull(warehouseId);
        this.productId = Objects.requireNonNull(productId);
        this.movementType = Objects.requireNonNull(movementType);
        this.quantity = Objects.requireNonNull(quantity);
        this.unitCost = Objects.requireNonNull(unitCost);
        this.totalValue = Objects.requireNonNull(totalValue);
        this.referenceId = Objects.requireNonNull(referenceId);
        this.lotNumber = lotNumber != null ? lotNumber : "";
        this.expirationDate = expirationDate;
        this.timestamp = Objects.requireNonNull(timestamp);
        this.performedBy = Objects.requireNonNull(performedBy);
    }

    public @Nullable Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getWarehouseId() { return warehouseId; }
    public Long getProductId() { return productId; }
    public InventoryMovementType getMovementType() { return movementType; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getTotalValue() { return totalValue; }
    public String getReferenceId() { return referenceId; }
    public String getLotNumber() { return lotNumber; }
    public @Nullable LocalDate getExpirationDate() { return expirationDate; }
    public Instant getTimestamp() { return timestamp; }
    public String getPerformedBy() { return performedBy; }
}
