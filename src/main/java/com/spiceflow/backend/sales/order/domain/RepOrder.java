package com.spiceflow.backend.sales.order.domain;

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
 * Root aggregate representing a Sales Representative Order (Rep Order).
 * Rule 15 compliant: copy-on-write immutability, defensive shop list copying, and self-validating invariants.
 */
public class RepOrder implements WorkflowAggregate<RepOrder, RepOrderState> {

    private @Nullable Long id;
    private String orderNumber;
    private Long tenantId;
    private Long repId;
    private LocalDate orderDate;
    private String routeArea;
    private RepOrderState state;
    private BigDecimal totalGrossAmount;
    private BigDecimal totalReturnsValue;
    private BigDecimal netAmount;
    private List<RepOrderShop> shops = new ArrayList<>();
    private String createdBy;
    private @Nullable String approvedBy;
    private @Nullable String loadedBy;
    private @Nullable String deliveredBy;
    private @Nullable Long version;
    private String correlationId;
    private Instant createdAt;
    private Instant updatedAt;
    private @Nullable Instant approvedAt;
    private @Nullable Instant loadedAt;
    private @Nullable Instant deliveredAt;

    protected RepOrder() {
        this.orderNumber = "";
        this.tenantId = 0L;
        this.repId = 0L;
        this.orderDate = LocalDate.now();
        this.routeArea = "";
        this.state = RepOrderState.DRAFT;
        this.totalGrossAmount = BigDecimal.ZERO;
        this.totalReturnsValue = BigDecimal.ZERO;
        this.netAmount = BigDecimal.ZERO;
        this.createdBy = "";
        this.correlationId = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    // COPY-ON-WRITE constructor (Rule 15 compliant)
    public RepOrder(RepOrder source,
                    RepOrderState newState,
                    @Nullable List<RepOrderShop> newShops) {
        this.id = source.id;
        this.orderNumber = source.orderNumber;
        this.tenantId = source.tenantId;
        this.repId = source.repId;
        this.orderDate = source.orderDate;
        this.routeArea = source.routeArea;
        this.createdBy = source.createdBy;
        this.approvedBy = source.approvedBy;
        this.loadedBy = source.loadedBy;
        this.deliveredBy = source.deliveredBy;
        this.createdAt = source.createdAt;
        this.updatedAt = Instant.now();
        this.approvedAt = source.approvedAt;
        this.loadedAt = source.loadedAt;
        this.deliveredAt = source.deliveredAt;
        this.version = source.version;
        this.correlationId = source.correlationId;

        this.shops = newShops != null ? new ArrayList<>(newShops) : new ArrayList<>(source.shops);

        this.totalGrossAmount = this.shops.stream()
                .map(RepOrderShop::grossOrderAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalReturnsValue = this.shops.stream()
                .map(RepOrderShop::returnsValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.netAmount = this.totalGrossAmount.subtract(this.totalReturnsValue);

        this.state = newState;
    }

    // Full constructor for persistence adapter restoration
    public RepOrder(@Nullable Long id, String orderNumber, Long tenantId, Long repId, LocalDate orderDate,
                    String routeArea, RepOrderState state, BigDecimal totalGrossAmount, BigDecimal totalReturnsValue,
                    BigDecimal netAmount, String createdBy, @Nullable String approvedBy, @Nullable String loadedBy,
                    @Nullable String deliveredBy, @Nullable Long version, String correlationId, Instant createdAt,
                    Instant updatedAt, @Nullable Instant approvedAt, @Nullable Instant loadedAt,
                    @Nullable Instant deliveredAt, List<RepOrderShop> shops) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.tenantId = tenantId;
        this.repId = repId;
        this.orderDate = orderDate;
        this.routeArea = routeArea;
        this.state = state;
        this.totalGrossAmount = totalGrossAmount;
        this.totalReturnsValue = totalReturnsValue;
        this.netAmount = netAmount;
        this.createdBy = createdBy;
        this.approvedBy = approvedBy;
        this.loadedBy = loadedBy;
        this.deliveredBy = deliveredBy;
        this.version = version;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.approvedAt = approvedAt;
        this.loadedAt = loadedAt;
        this.deliveredAt = deliveredAt;
        this.shops = new ArrayList<>(shops);
    }

    public static RepOrder create(String orderNumber, Long tenantId, Long repId, LocalDate orderDate,
                                  String routeArea, String createdBy) {
        RepOrder ro = new RepOrder();
        ro.orderNumber = orderNumber;
        ro.tenantId = tenantId;
        ro.repId = repId;
        ro.orderDate = orderDate;
        ro.routeArea = routeArea != null ? routeArea : "";
        ro.state = RepOrderState.DRAFT;
        ro.totalGrossAmount = BigDecimal.ZERO;
        ro.totalReturnsValue = BigDecimal.ZERO;
        ro.netAmount = BigDecimal.ZERO;
        ro.createdBy = createdBy;
        ro.shops = new ArrayList<>();
        ro.correlationId = orderNumber;
        ro.createdAt = Instant.now();
        ro.updatedAt = Instant.now();
        return ro;
    }

    @Override
    public String getAggregateId() {
        return orderNumber;
    }

    @Override
    public RepOrderState getWorkflowState() {
        return state;
    }

    @Override
    public WorkflowTransitionOutput<RepOrder> transitionTo(RepOrderState targetState, WorkflowContext context) {
        RepOrder copy = new RepOrder(this, targetState, this.shops);
        if (targetState == RepOrderState.APPROVED && copy.approvedAt == null) {
            copy.approvedAt = context.timestamp();
            copy.approvedBy = String.valueOf(context.userId());
        }
        if (targetState == RepOrderState.LOADED && copy.loadedAt == null) {
            copy.loadedAt = context.timestamp();
            copy.loadedBy = String.valueOf(context.userId());
        }
        if (targetState == RepOrderState.DELIVERED && copy.deliveredAt == null) {
            copy.deliveredAt = context.timestamp();
            copy.deliveredBy = String.valueOf(context.userId());
        }

        DomainEventType eventType = switch (targetState) {
            case DRAFT -> DomainEventType.REP_ORDER_CREATED;
            case SUBMITTED -> DomainEventType.REP_ORDER_SUBMITTED;
            case APPROVED -> DomainEventType.REP_ORDER_APPROVED;
            case LOADED -> DomainEventType.REP_ORDER_LOADED;
            case DELIVERED -> DomainEventType.REP_ORDER_DELIVERED;
            case CANCELLED -> DomainEventType.REP_ORDER_CANCELLED;
        };

        RepOrderEvent event = new RepOrderEvent(
                this.orderNumber,
                eventType,
                context.correlationId(),
                context.timestamp(),
                context.tenantId()
        );
        return new WorkflowTransitionOutput<>(copy, List.of(event));
    }

    // GETTERS ONLY (NO SETTERS — STRICT RULE 15)
    public @Nullable Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Long getTenantId() { return tenantId; }
    public Long getRepId() { return repId; }
    public LocalDate getOrderDate() { return orderDate; }
    public String getRouteArea() { return routeArea; }
    public RepOrderState getState() { return state; }
    public BigDecimal getTotalGrossAmount() { return totalGrossAmount; }
    public BigDecimal getTotalReturnsValue() { return totalReturnsValue; }
    public BigDecimal getNetAmount() { return netAmount; }
    public List<RepOrderShop> getShops() { return Collections.unmodifiableList(shops); }
    public String getCreatedBy() { return createdBy; }
    public @Nullable String getApprovedBy() { return approvedBy; }
    public @Nullable String getLoadedBy() { return loadedBy; }
    public @Nullable String getDeliveredBy() { return deliveredBy; }
    public @Nullable Long getVersion() { return version; }
    public String getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public @Nullable Instant getApprovedAt() { return approvedAt; }
    public @Nullable Instant getLoadedAt() { return loadedAt; }
    public @Nullable Instant getDeliveredAt() { return deliveredAt; }
}
