package com.spiceflow.backend.purchasing.domain;

import com.spiceflow.backend.events.DomainEventType;
import com.spiceflow.backend.workflow.WorkflowAggregate;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowTransitionOutput;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class PurchaseOrder implements WorkflowAggregate<PurchaseOrder, PurchaseOrderState> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(nullable = false, unique = true)
    private String poNumber;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Long supplierId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "purchase_order_id")
    private List<PurchaseOrderLine> lines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PurchaseOrderState state;

    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Version
    private @Nullable Long version;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PurchaseOrder() {
        // JPA only
        this.poNumber = "";
        this.tenantId = 0L;
        this.supplierId = 0L;
        this.state = PurchaseOrderState.DRAFT;
        this.orderDate = Instant.EPOCH;
        this.totalAmount = BigDecimal.ZERO;
        this.createdBy = "";
        this.correlationId = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    // COPY-ON-WRITE constructor (Rule 15 compliant)
    public PurchaseOrder(PurchaseOrder source,
                         PurchaseOrderState newState,
                         List<PurchaseOrderLine> newLines) {

        this.id = source.id;
        this.poNumber = source.poNumber;
        this.tenantId = source.tenantId;
        this.supplierId = source.supplierId;
        this.orderDate = source.orderDate;
        this.createdBy = source.createdBy;
        this.createdAt = source.createdAt;
        this.updatedAt = Instant.now();

        // IMPORTANT: defensive copy (no shared references)
        this.lines = newLines != null
                ? new ArrayList<>(newLines)
                : new ArrayList<>(source.lines);

        this.totalAmount = this.lines.stream()
                .map(PurchaseOrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.state = newState;
        this.version = source.version;
        this.correlationId = source.correlationId;
    }

    // Full constructor for persistence adapter restoration
    public PurchaseOrder(@Nullable Long id, String poNumber, Long tenantId, Long supplierId,
                         PurchaseOrderState state, Instant orderDate, BigDecimal totalAmount,
                         String createdBy, @Nullable Long version, String correlationId,
                         Instant createdAt, Instant updatedAt, List<PurchaseOrderLine> lines) {
        this.id = id;
        this.poNumber = poNumber;
        this.tenantId = tenantId;
        this.supplierId = supplierId;
        this.state = state;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.createdBy = createdBy;
        this.version = version;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lines = new ArrayList<>(lines);
    }

    public static PurchaseOrder create(Long supplierId, String poNumber) {
        return create(supplierId, poNumber, 1L, "system");
    }

    public static PurchaseOrder create(Long supplierId, String poNumber, Long tenantId, String createdBy) {
        PurchaseOrder po = new PurchaseOrder();
        po.supplierId = supplierId;
        po.poNumber = poNumber;
        po.tenantId = tenantId;
        po.state = PurchaseOrderState.DRAFT;
        po.orderDate = Instant.now();
        po.totalAmount = BigDecimal.ZERO;
        po.createdBy = createdBy;
        po.lines = new ArrayList<>();
        po.correlationId = poNumber;
        po.createdAt = Instant.now();
        po.updatedAt = Instant.now();
        return po;
    }

    @Override
    public String getAggregateId() {
        return poNumber;
    }

    @Override
    public PurchaseOrderState getWorkflowState() {
        return state;
    }

    @Override
    public WorkflowTransitionOutput<PurchaseOrder> transitionTo(PurchaseOrderState targetState, WorkflowContext context) {
        PurchaseOrder copy = new PurchaseOrder(this, targetState, this.lines);
        DomainEventType eventType = switch (targetState) {
            case SUBMITTED -> DomainEventType.PURCHASE_ORDER_SUBMITTED;
            case APPROVED -> DomainEventType.PURCHASE_ORDER_APPROVED;
            case REJECTED -> DomainEventType.PURCHASE_ORDER_REJECTED;
            case ORDERED -> DomainEventType.PURCHASE_ORDER_ORDERED;
            case PARTIALLY_RECEIVED -> DomainEventType.PURCHASE_ORDER_PARTIALLY_RECEIVED;
            case RECEIVED -> DomainEventType.PURCHASE_ORDER_RECEIVED;
            case CLOSED -> DomainEventType.PURCHASE_ORDER_CLOSED;
            case DRAFT -> DomainEventType.PURCHASE_ORDER_CREATED;
        };
        PurchaseOrderEvent event = new PurchaseOrderEvent(
                this.poNumber,
                eventType,
                context.correlationId(),
                context.timestamp(),
                context.tenantId()
        );
        return new WorkflowTransitionOutput<>(copy, List.of(event));
    }

    // GETTERS ONLY (NO SETTERS — STRICT RULE 15)
    public @Nullable Long getId() { return id; }
    public String getPoNumber() { return poNumber; }
    public Long getTenantId() { return tenantId; }
    public Long getSupplierId() { return supplierId; }
    public List<PurchaseOrderLine> getLines() { return lines; }
    public PurchaseOrderState getState() { return state; }
    public Instant getOrderDate() { return orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCreatedBy() { return createdBy; }
    public @Nullable Long getVersion() { return version; }
    public String getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
