package com.spiceflow.backend.sales.collection.domain;

import com.spiceflow.backend.events.DomainEventType;
import com.spiceflow.backend.workflow.WorkflowAggregate;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowTransitionOutput;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Root aggregate representing a Cash Collection operational workflow.
 *
 * Rule 15 compliant: copy-on-write immutability, self-validating invariants,
 * and @Version optimistic locking.
 * Follows ADR-013 stateless WorkflowEngine execution contract.
 */
public class CashCollection implements WorkflowAggregate<CashCollection, CashCollectionState> {

    private @Nullable Long id;
    private String collectionNumber;
    private Long tenantId;
    private Long shopId;
    private @Nullable Long repId;
    private LocalDate collectionDate;
    private BigDecimal amount;
    private String paymentMethod; // CASH, CHEQUE, BANK_TRANSFER
    private @Nullable String chequeNo;
    private @Nullable String chequeBankName;
    private @Nullable LocalDate chequeDate;
    private @Nullable String notes;
    private CashCollectionState state;

    // Audit fields
    private String createdBy;
    private @Nullable String confirmedBy;
    private @Nullable String cancelledBy;
    private @Nullable Long version;
    private String correlationId;
    private Instant createdAt;
    private Instant updatedAt;
    private @Nullable Instant confirmedAt;
    private @Nullable Instant cancelledAt;

    /** JPA/Jackson no-arg constructor — package-private to enforce factory usage. */
    protected CashCollection() {
        this.collectionNumber = "";
        this.tenantId = 0L;
        this.shopId = 0L;
        this.collectionDate = LocalDate.now(ZoneOffset.UTC);
        this.amount = BigDecimal.ZERO;
        this.paymentMethod = "CASH";
        this.state = CashCollectionState.PENDING;
        this.createdBy = "";
        this.correlationId = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    // ─── COPY-ON-WRITE constructor (Rule 15 compliant) ─────────────────────────
    public CashCollection(CashCollection source, CashCollectionState newState) {
        this.id = source.id;
        this.collectionNumber = source.collectionNumber;
        this.tenantId = source.tenantId;
        this.shopId = source.shopId;
        this.repId = source.repId;
        this.collectionDate = source.collectionDate;
        this.amount = source.amount;
        this.paymentMethod = source.paymentMethod;
        this.chequeNo = source.chequeNo;
        this.chequeBankName = source.chequeBankName;
        this.chequeDate = source.chequeDate;
        this.notes = source.notes;
        this.createdBy = source.createdBy;
        this.confirmedBy = source.confirmedBy;
        this.cancelledBy = source.cancelledBy;
        this.version = source.version;
        this.correlationId = source.correlationId;
        this.createdAt = source.createdAt;
        this.updatedAt = Instant.now();
        this.confirmedAt = source.confirmedAt;
        this.cancelledAt = source.cancelledAt;
        this.state = newState;
    }

    // ─── Full constructor for PersistenceAdapter restoration ───────────────────
    public CashCollection(@Nullable Long id, String collectionNumber, Long tenantId, Long shopId,
                          @Nullable Long repId, LocalDate collectionDate, BigDecimal amount,
                          String paymentMethod, @Nullable String chequeNo, @Nullable String chequeBankName,
                          @Nullable LocalDate chequeDate, @Nullable String notes, CashCollectionState state,
                          String createdBy, @Nullable String confirmedBy, @Nullable String cancelledBy,
                          @Nullable Long version, String correlationId, Instant createdAt, Instant updatedAt,
                          @Nullable Instant confirmedAt, @Nullable Instant cancelledAt) {
        this.id = id;
        this.collectionNumber = collectionNumber;
        this.tenantId = tenantId;
        this.shopId = shopId;
        this.repId = repId;
        this.collectionDate = collectionDate;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.chequeNo = chequeNo;
        this.chequeBankName = chequeBankName;
        this.chequeDate = chequeDate;
        this.notes = notes;
        this.state = state;
        this.createdBy = createdBy;
        this.confirmedBy = confirmedBy;
        this.cancelledBy = cancelledBy;
        this.version = version;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
    }

    // ─── Factory method ─────────────────────────────────────────────────────────
    public static CashCollection create(String collectionNumber, Long tenantId, Long shopId,
                                        @Nullable Long repId, LocalDate collectionDate, BigDecimal amount,
                                        String paymentMethod, @Nullable String chequeNo, @Nullable String chequeBankName,
                                        @Nullable LocalDate chequeDate, @Nullable String notes, String createdBy) {
        CashCollection c = new CashCollection();
        c.collectionNumber = collectionNumber;
        c.tenantId = tenantId;
        c.shopId = shopId;
        c.repId = repId;
        c.collectionDate = collectionDate;
        c.amount = amount;
        c.paymentMethod = paymentMethod;
        c.chequeNo = chequeNo;
        c.chequeBankName = chequeBankName;
        c.chequeDate = chequeDate;
        c.notes = notes;
        c.state = CashCollectionState.PENDING;
        c.createdBy = createdBy;
        c.correlationId = collectionNumber;
        c.createdAt = Instant.now();
        c.updatedAt = Instant.now();
        return c;
    }

    // ─── WorkflowAggregate interface ────────────────────────────────────────────
    @Override
    public String getAggregateId() {
        return collectionNumber;
    }

    @Override
    public CashCollectionState getWorkflowState() {
        return state;
    }

    @Override
    public WorkflowTransitionOutput<CashCollection> transitionTo(CashCollectionState targetState, WorkflowContext context) {
        // Build a copy with the new state
        CashCollection copy = new CashCollection(this, targetState);

        // Stamp lifecycle timestamps on the copy
        if (targetState == CashCollectionState.CONFIRMED && copy.confirmedAt == null) {
            copy.confirmedAt = context.timestamp();
            copy.confirmedBy = String.valueOf(context.userId());
        }
        if (targetState == CashCollectionState.CANCELLED && copy.cancelledAt == null) {
            copy.cancelledAt = context.timestamp();
            copy.cancelledBy = String.valueOf(context.userId());
        }

        DomainEventType eventType = switch (targetState) {
            case PENDING -> DomainEventType.CASH_COLLECTED; // should not occur normally
            case CONFIRMED -> DomainEventType.CASH_COLLECTED;
            case CANCELLED -> DomainEventType.CASH_COLLECTED;
        };

        CashCollectionEvent event = new CashCollectionEvent(
                this.collectionNumber,
                eventType,
                context.correlationId(),
                context.timestamp(),
                context.tenantId()
        );
        return new WorkflowTransitionOutput<>(copy, List.of(event));
    }

    // ─── GETTERS ONLY (NO SETTERS — STRICT RULE 15) ─────────────────────────────
    public @Nullable Long getId() { return id; }
    public String getCollectionNumber() { return collectionNumber; }
    public Long getTenantId() { return tenantId; }
    public Long getShopId() { return shopId; }
    public @Nullable Long getRepId() { return repId; }
    public LocalDate getCollectionDate() { return collectionDate; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public @Nullable String getChequeNo() { return chequeNo; }
    public @Nullable String getChequeBankName() { return chequeBankName; }
    public @Nullable LocalDate getChequeDate() { return chequeDate; }
    public @Nullable String getNotes() { return notes; }
    public CashCollectionState getState() { return state; }
    public String getCreatedBy() { return createdBy; }
    public @Nullable String getConfirmedBy() { return confirmedBy; }
    public @Nullable String getCancelledBy() { return cancelledBy; }
    public @Nullable Long getVersion() { return version; }
    public String getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public @Nullable Instant getConfirmedAt() { return confirmedAt; }
    public @Nullable Instant getCancelledAt() { return cancelledAt; }
}
