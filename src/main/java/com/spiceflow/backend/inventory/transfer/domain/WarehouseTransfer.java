package com.spiceflow.backend.inventory.transfer.domain;

import com.spiceflow.backend.events.DomainEventType;
import com.spiceflow.backend.workflow.WorkflowAggregate;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowTransitionOutput;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class WarehouseTransfer implements WorkflowAggregate<WarehouseTransfer, WarehouseTransferState> {

    private @Nullable Long id;
    private String transferNumber;
    private Long tenantId;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private List<WarehouseTransferLine> lines = new ArrayList<>();
    private WarehouseTransferState state;
    private Instant requestDate;
    private BigDecimal totalTransferValue;
    private String createdBy;
    private @Nullable String approvedBy;
    private @Nullable String shippedBy;
    private @Nullable String receivedBy;
    private @Nullable Long version;
    private String correlationId;
    private Instant createdAt;
    private Instant updatedAt;
    private @Nullable Instant approvedAt;
    private @Nullable Instant shippedAt;
    private @Nullable Instant receivedAt;

    protected WarehouseTransfer() {
        this.transferNumber = "";
        this.tenantId = 0L;
        this.fromWarehouseId = 0L;
        this.toWarehouseId = 0L;
        this.state = WarehouseTransferState.DRAFT;
        this.requestDate = Instant.EPOCH;
        this.totalTransferValue = BigDecimal.ZERO;
        this.createdBy = "";
        this.correlationId = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    // COPY-ON-WRITE constructor (Rule 15 compliant)
    public WarehouseTransfer(WarehouseTransfer source,
                             WarehouseTransferState newState,
                             @Nullable List<WarehouseTransferLine> newLines) {
        this.id = source.id;
        this.transferNumber = source.transferNumber;
        this.tenantId = source.tenantId;
        this.fromWarehouseId = source.fromWarehouseId;
        this.toWarehouseId = source.toWarehouseId;
        this.requestDate = source.requestDate;
        this.createdBy = source.createdBy;
        this.approvedBy = source.approvedBy;
        this.shippedBy = source.shippedBy;
        this.receivedBy = source.receivedBy;
        this.createdAt = source.createdAt;
        this.updatedAt = Instant.now();
        this.approvedAt = source.approvedAt;
        this.shippedAt = source.shippedAt;
        this.receivedAt = source.receivedAt;

        // Defensive copy (no shared references)
        this.lines = newLines != null
                ? new ArrayList<>(newLines)
                : new ArrayList<>(source.lines);

        this.totalTransferValue = this.lines.stream()
                .map(line -> line.getShippedQty().compareTo(BigDecimal.ZERO) > 0
                        ? line.getShippedQty().multiply(line.getUnitCost())
                        : line.getRequestedQty().multiply(line.getUnitCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.state = newState;
        this.version = source.version;
        this.correlationId = source.correlationId;
    }

    // Full constructor for persistence adapter restoration
    public WarehouseTransfer(@Nullable Long id, String transferNumber, Long tenantId, Long fromWarehouseId,
                             Long toWarehouseId, WarehouseTransferState state, Instant requestDate,
                             BigDecimal totalTransferValue, String createdBy, @Nullable String approvedBy,
                             @Nullable String shippedBy, @Nullable String receivedBy, @Nullable Long version,
                             String correlationId, Instant createdAt, Instant updatedAt, @Nullable Instant approvedAt,
                             @Nullable Instant shippedAt, @Nullable Instant receivedAt, List<WarehouseTransferLine> lines) {
        this.id = id;
        this.transferNumber = transferNumber;
        this.tenantId = tenantId;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.state = state;
        this.requestDate = requestDate;
        this.totalTransferValue = totalTransferValue;
        this.createdBy = createdBy;
        this.approvedBy = approvedBy;
        this.shippedBy = shippedBy;
        this.receivedBy = receivedBy;
        this.version = version;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.approvedAt = approvedAt;
        this.shippedAt = shippedAt;
        this.receivedAt = receivedAt;
        this.lines = new ArrayList<>(lines);
    }

    public static WarehouseTransfer create(String transferNumber, Long tenantId, Long fromWarehouseId,
                                           Long toWarehouseId, String createdBy) {
        WarehouseTransfer wt = new WarehouseTransfer();
        wt.transferNumber = transferNumber;
        wt.tenantId = tenantId;
        wt.fromWarehouseId = fromWarehouseId;
        wt.toWarehouseId = toWarehouseId;
        wt.state = WarehouseTransferState.DRAFT;
        wt.requestDate = Instant.now();
        wt.totalTransferValue = BigDecimal.ZERO;
        wt.createdBy = createdBy;
        wt.lines = new ArrayList<>();
        wt.correlationId = transferNumber;
        wt.createdAt = Instant.now();
        wt.updatedAt = Instant.now();
        return wt;
    }

    @Override
    public String getAggregateId() {
        return transferNumber;
    }

    @Override
    public WarehouseTransferState getWorkflowState() {
        return state;
    }

    @Override
    public WorkflowTransitionOutput<WarehouseTransfer> transitionTo(WarehouseTransferState targetState, WorkflowContext context) {
        WarehouseTransfer copy = new WarehouseTransfer(this, targetState, this.lines);
        if (targetState == WarehouseTransferState.APPROVED && copy.approvedAt == null) {
            copy.approvedAt = context.timestamp();
            copy.approvedBy = String.valueOf(context.userId());
        }
        if (targetState == WarehouseTransferState.SHIPPED && copy.shippedAt == null) {
            copy.shippedAt = context.timestamp();
            copy.shippedBy = String.valueOf(context.userId());
        }
        if (targetState == WarehouseTransferState.RECEIVED && copy.receivedAt == null) {
            copy.receivedAt = context.timestamp();
            copy.receivedBy = String.valueOf(context.userId());
        }

        DomainEventType eventType = switch (targetState) {
            case DRAFT, REQUESTED -> DomainEventType.WAREHOUSE_TRANSFER_REQUESTED;
            case APPROVED -> DomainEventType.WAREHOUSE_TRANSFER_APPROVED;
            case SHIPPED -> DomainEventType.WAREHOUSE_TRANSFER_SHIPPED;
            case RECEIVED -> DomainEventType.WAREHOUSE_TRANSFER_RECEIVED;
            case CANCELLED -> DomainEventType.WAREHOUSE_TRANSFER_CANCELLED;
        };

        WarehouseTransferEvent event = new WarehouseTransferEvent(
                this.transferNumber,
                eventType,
                context.correlationId(),
                context.timestamp(),
                context.tenantId()
        );
        return new WorkflowTransitionOutput<>(copy, List.of(event));
    }

    // GETTERS ONLY (NO SETTERS — STRICT RULE 15)
    public @Nullable Long getId() { return id; }
    public String getTransferNumber() { return transferNumber; }
    public Long getTenantId() { return tenantId; }
    public Long getFromWarehouseId() { return fromWarehouseId; }
    public Long getToWarehouseId() { return toWarehouseId; }
    public List<WarehouseTransferLine> getLines() { return lines; }
    public WarehouseTransferState getState() { return state; }
    public Instant getRequestDate() { return requestDate; }
    public BigDecimal getTotalTransferValue() { return totalTransferValue; }
    public String getCreatedBy() { return createdBy; }
    public @Nullable String getApprovedBy() { return approvedBy; }
    public @Nullable String getShippedBy() { return shippedBy; }
    public @Nullable String getReceivedBy() { return receivedBy; }
    public @Nullable Long getVersion() { return version; }
    public String getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public @Nullable Instant getApprovedAt() { return approvedAt; }
    public @Nullable Instant getShippedAt() { return shippedAt; }
    public @Nullable Instant getReceivedAt() { return receivedAt; }
}
