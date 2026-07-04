package com.spiceflow.backend.sales.delivery.domain;

import com.spiceflow.backend.events.DomainEventType;
import com.spiceflow.backend.workflow.WorkflowAggregate;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowTransitionOutput;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Root aggregate representing a Customer Delivery operational workflow.
 *
 * Rule 15 compliant: copy-on-write immutability, defensive shop list copying,
 * self-validating invariants, and @Version optimistic locking.
 * Follows ADR-013 stateless WorkflowEngine execution contract.
 */
public class Delivery implements WorkflowAggregate<Delivery, DeliveryState> {

    private @Nullable Long id;
    private String deliveryNumber;
    private Long tenantId;
    private Long loadingSheetId;
    private @Nullable String loadingSheetNumber;
    private LocalDate deliveryDate;
    private DeliveryState state;

    // Financial summary (computed on completion)
    private BigDecimal totalSalesValue;
    private BigDecimal totalReturnsValue;
    private BigDecimal totalCollectedAmount;

    // Shop delivery records (one per customer shop visited)
    private List<DeliveryShopRecord> shops;

    // Audit fields
    private String createdBy;
    private @Nullable String dispatchedBy;
    private @Nullable String completedBy;
    private @Nullable String cancelledBy;
    private @Nullable Long version;
    private String correlationId;
    private Instant createdAt;
    private Instant updatedAt;
    private @Nullable Instant dispatchedAt;
    private @Nullable Instant completedAt;
    private @Nullable Instant cancelledAt;

    /** JPA/Jackson no-arg constructor — package-private to enforce factory usage. */
    protected Delivery() {
        this.deliveryNumber = "";
        this.tenantId = 0L;
        this.loadingSheetId = 0L;
        this.deliveryDate = LocalDate.now();
        this.state = DeliveryState.IN_PROGRESS;
        this.totalSalesValue = BigDecimal.ZERO;
        this.totalReturnsValue = BigDecimal.ZERO;
        this.totalCollectedAmount = BigDecimal.ZERO;
        this.shops = new ArrayList<>();
        this.createdBy = "";
        this.correlationId = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    // ─── COPY-ON-WRITE constructor (Rule 15 compliant) ─────────────────────────
    public Delivery(Delivery source, DeliveryState newState, @Nullable List<DeliveryShopRecord> newShops) {
        this.id = source.id;
        this.deliveryNumber = source.deliveryNumber;
        this.tenantId = source.tenantId;
        this.loadingSheetId = source.loadingSheetId;
        this.loadingSheetNumber = source.loadingSheetNumber;
        this.deliveryDate = source.deliveryDate;
        this.totalSalesValue = source.totalSalesValue;
        this.totalReturnsValue = source.totalReturnsValue;
        this.totalCollectedAmount = source.totalCollectedAmount;
        this.createdBy = source.createdBy;
        this.dispatchedBy = source.dispatchedBy;
        this.completedBy = source.completedBy;
        this.cancelledBy = source.cancelledBy;
        this.version = source.version;
        this.correlationId = source.correlationId;
        this.createdAt = source.createdAt;
        this.updatedAt = Instant.now();
        this.dispatchedAt = source.dispatchedAt;
        this.completedAt = source.completedAt;
        this.cancelledAt = source.cancelledAt;
        this.shops = newShops != null ? new ArrayList<>(newShops) : new ArrayList<>(source.shops);
        this.state = newState;
    }

    // ─── Full constructor for PersistenceAdapter restoration ───────────────────
    public Delivery(@Nullable Long id, String deliveryNumber, Long tenantId, Long loadingSheetId,
                    @Nullable String loadingSheetNumber, LocalDate deliveryDate, DeliveryState state,
                    BigDecimal totalSalesValue, BigDecimal totalReturnsValue, BigDecimal totalCollectedAmount,
                    String createdBy, @Nullable String dispatchedBy, @Nullable String completedBy,
                    @Nullable String cancelledBy, @Nullable Long version, String correlationId,
                    Instant createdAt, Instant updatedAt, @Nullable Instant dispatchedAt,
                    @Nullable Instant completedAt, @Nullable Instant cancelledAt,
                    List<DeliveryShopRecord> shops) {
        this.id = id;
        this.deliveryNumber = deliveryNumber;
        this.tenantId = tenantId;
        this.loadingSheetId = loadingSheetId;
        this.loadingSheetNumber = loadingSheetNumber;
        this.deliveryDate = deliveryDate;
        this.state = state;
        this.totalSalesValue = totalSalesValue;
        this.totalReturnsValue = totalReturnsValue;
        this.totalCollectedAmount = totalCollectedAmount;
        this.createdBy = createdBy;
        this.dispatchedBy = dispatchedBy;
        this.completedBy = completedBy;
        this.cancelledBy = cancelledBy;
        this.version = version;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.dispatchedAt = dispatchedAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        this.shops = new ArrayList<>(shops);
    }

    // ─── Factory method ─────────────────────────────────────────────────────────
    public static Delivery create(String deliveryNumber, Long tenantId, Long loadingSheetId,
                                   @Nullable String loadingSheetNumber, LocalDate deliveryDate,
                                   String createdBy, List<DeliveryShopRecord> shops) {
        Delivery d = new Delivery();
        d.deliveryNumber = deliveryNumber;
        d.tenantId = tenantId;
        d.loadingSheetId = loadingSheetId;
        d.loadingSheetNumber = loadingSheetNumber != null ? loadingSheetNumber : "";
        d.deliveryDate = deliveryDate;
        d.state = DeliveryState.IN_PROGRESS;
        d.createdBy = createdBy;
        d.shops = new ArrayList<>(shops);
        d.correlationId = deliveryNumber;
        d.createdAt = Instant.now();
        d.updatedAt = Instant.now();
        return d;
    }

    // ─── WorkflowAggregate interface ────────────────────────────────────────────
    @Override
    public String getAggregateId() {
        return deliveryNumber;
    }

    @Override
    public DeliveryState getWorkflowState() {
        return state;
    }

    @Override
    public WorkflowTransitionOutput<Delivery> transitionTo(DeliveryState targetState, WorkflowContext context) {
        // Build a copy with the new state
        Delivery copy = new Delivery(this, targetState, null);

        // Stamp lifecycle timestamps on the copy
        if (targetState == DeliveryState.DISPATCHED && copy.dispatchedAt == null) {
            copy.dispatchedAt = context.timestamp();
            copy.dispatchedBy = String.valueOf(context.userId());
        }
        if (targetState == DeliveryState.COMPLETED && copy.completedAt == null) {
            copy.completedAt = context.timestamp();
            copy.completedBy = String.valueOf(context.userId());
        }
        if (targetState == DeliveryState.CANCELLED && copy.cancelledAt == null) {
            copy.cancelledAt = context.timestamp();
            copy.cancelledBy = String.valueOf(context.userId());
        }

        DomainEventType eventType = switch (targetState) {
            case IN_PROGRESS -> DomainEventType.DELIVERY_COMPLETED; // should not occur normally
            case DISPATCHED -> DomainEventType.DELIVERY_DISPATCHED;
            case COMPLETED -> DomainEventType.DELIVERY_COMPLETED;
            case CANCELLED -> DomainEventType.DELIVERY_CANCELLED;
        };

        DeliveryEvent event = new DeliveryEvent(
                this.deliveryNumber,
                eventType,
                context.correlationId(),
                context.timestamp(),
                context.tenantId()
        );
        return new WorkflowTransitionOutput<>(copy, List.of(event));
    }

    // ─── GETTERS ONLY (NO SETTERS — STRICT RULE 15) ─────────────────────────────
    public @Nullable Long getId() { return id; }
    public String getDeliveryNumber() { return deliveryNumber; }
    public Long getTenantId() { return tenantId; }
    public Long getLoadingSheetId() { return loadingSheetId; }
    public @Nullable String getLoadingSheetNumber() { return loadingSheetNumber; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public DeliveryState getState() { return state; }
    public BigDecimal getTotalSalesValue() { return totalSalesValue; }
    public BigDecimal getTotalReturnsValue() { return totalReturnsValue; }
    public BigDecimal getTotalCollectedAmount() { return totalCollectedAmount; }
    public List<DeliveryShopRecord> getShops() { return Collections.unmodifiableList(shops); }
    public String getCreatedBy() { return createdBy; }
    public @Nullable String getDispatchedBy() { return dispatchedBy; }
    public @Nullable String getCompletedBy() { return completedBy; }
    public @Nullable String getCancelledBy() { return cancelledBy; }
    public @Nullable Long getVersion() { return version; }
    public String getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public @Nullable Instant getDispatchedAt() { return dispatchedAt; }
    public @Nullable Instant getCompletedAt() { return completedAt; }
    public @Nullable Instant getCancelledAt() { return cancelledAt; }

    /** Package-internal: called by DeliveryWorkflowService to set financial totals on completion. */
    void setTotalSalesValue(BigDecimal totalSalesValue) { this.totalSalesValue = totalSalesValue; }
    void setTotalReturnsValue(BigDecimal totalReturnsValue) { this.totalReturnsValue = totalReturnsValue; }
    void setTotalCollectedAmount(BigDecimal totalCollectedAmount) { this.totalCollectedAmount = totalCollectedAmount; }
}
