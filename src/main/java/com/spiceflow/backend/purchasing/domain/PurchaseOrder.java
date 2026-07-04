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
    private Long supplierId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "purchase_order_id")
    private List<PurchaseOrderLine> lines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PurchaseOrderState state;

    @Version
    private @Nullable Long version;

    @Column(nullable = false)
    private String correlationId;

    protected PurchaseOrder() {
        // JPA only
        this.poNumber = "";
        this.supplierId = 0L;
        this.state = PurchaseOrderState.DRAFT;
        this.correlationId = "";
    }

    // COPY-ON-WRITE constructor (Rule 15 compliant)
    public PurchaseOrder(PurchaseOrder source,
                         PurchaseOrderState newState,
                         List<PurchaseOrderLine> newLines) {

        this.id = source.id;
        this.poNumber = source.poNumber;
        this.supplierId = source.supplierId;

        // IMPORTANT: defensive copy (no shared references)
        this.lines = newLines != null
                ? new ArrayList<>(newLines)
                : new ArrayList<>(source.lines);

        this.state = newState;
        this.version = source.version;
        this.correlationId = source.correlationId;
    }

    public static PurchaseOrder create(Long supplierId, String poNumber) {
        PurchaseOrder po = new PurchaseOrder();
        po.supplierId = supplierId;
        po.poNumber = poNumber;
        po.state = PurchaseOrderState.DRAFT;
        po.lines = new ArrayList<>();
        po.correlationId = poNumber;
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
    public Long getSupplierId() { return supplierId; }
    public List<PurchaseOrderLine> getLines() { return lines; }
    public PurchaseOrderState getState() { return state; }
    public @Nullable Long getVersion() { return version; }
    public String getCorrelationId() { return correlationId; }
}
