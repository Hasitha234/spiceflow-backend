package com.spiceflow.backend.sales.collection.entity;

import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.jspecify.annotations.Nullable;

/**
 * JPA entity mapping the {@code cash_collections} table for the workflow execution layer.
 * This entity is exclusively used by {@code CashCollectionPersistenceAdapter} and must
 * never be exposed to or manipulated by services directly.
 */
@Entity
@Table(name = "cash_collections")
public class CashCollectionWorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "collection_number", nullable = false, unique = true)
    private String collectionNumber = "";

    @Column(name = "correlation_id", nullable = false)
    private String correlationId = "";

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId = 0L;

    @Column(name = "shop_id", nullable = false)
    private Long shopId = 0L;

    @Column(name = "rep_id")
    private @Nullable Long repId;

    @Column(name = "collection_date", nullable = false)
    private LocalDate collectionDate = LocalDate.now(ZoneOffset.UTC);

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod = "CASH";

    @Column(name = "cheque_no")
    private @Nullable String chequeNo;

    @Column(name = "cheque_bank_name")
    private @Nullable String chequeBankName;

    @Column(name = "cheque_date")
    private @Nullable LocalDate chequeDate;

    @Column(name = "notes")
    private @Nullable String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CashCollectionState status = CashCollectionState.PENDING;

    @Column(name = "created_by", nullable = false)
    private String createdBy = "";

    @Column(name = "confirmed_by")
    private @Nullable String confirmedBy;

    @Column(name = "cancelled_by")
    private @Nullable String cancelledBy;

    @Version
    private @Nullable Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.EPOCH;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.EPOCH;

    @Column(name = "confirmed_at")
    private @Nullable Instant confirmedAt;

    @Column(name = "cancelled_at")
    private @Nullable Instant cancelledAt;

    public CashCollectionWorkflowEntity() {
    }

    public @Nullable Long getId() { return id; }
    public void setId(@Nullable Long id) { this.id = id; }

    public String getCollectionNumber() { return collectionNumber; }
    public void setCollectionNumber(String collectionNumber) { this.collectionNumber = collectionNumber; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }

    public @Nullable Long getRepId() { return repId; }
    public void setRepId(@Nullable Long repId) { this.repId = repId; }

    public LocalDate getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public @Nullable String getChequeNo() { return chequeNo; }
    public void setChequeNo(@Nullable String chequeNo) { this.chequeNo = chequeNo; }

    public @Nullable String getChequeBankName() { return chequeBankName; }
    public void setChequeBankName(@Nullable String chequeBankName) { this.chequeBankName = chequeBankName; }

    public @Nullable LocalDate getChequeDate() { return chequeDate; }
    public void setChequeDate(@Nullable LocalDate chequeDate) { this.chequeDate = chequeDate; }

    public @Nullable String getNotes() { return notes; }
    public void setNotes(@Nullable String notes) { this.notes = notes; }

    public CashCollectionState getStatus() { return status; }
    public void setStatus(CashCollectionState status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

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
}
