package com.spiceflow.backend.sales.loading.domain;

import com.spiceflow.backend.events.DomainEventType;
import com.spiceflow.backend.workflow.WorkflowAggregate;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowTransitionOutput;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Root aggregate representing a Van Loading Sheet.
 * Rule 15 compliant: copy-on-write immutability, defensive item/return list copying, and self-validating invariants.
 */
public class LoadingSheet implements WorkflowAggregate<LoadingSheet, LoadingSheetState> {

    private @Nullable Long id;
    private String sheetNumber;
    private Long tenantId;
    private Long repOrderId;
    private String repOrderNumber;
    private Long driverId;
    private String driverName;
    private LocalDate loadingDate;
    private LoadingSheetState state;
    private List<LoadingSheetItem> items = new ArrayList<>();
    private List<LoadingSheetReturnItem> returns = new ArrayList<>();
    private String createdBy;
    private @Nullable String confirmedBy;
    private @Nullable String cancelledBy;
    private @Nullable Long version;
    private String correlationId;
    private Instant createdAt;
    private Instant updatedAt;
    private @Nullable Instant confirmedAt;
    private @Nullable Instant cancelledAt;

    protected LoadingSheet() {
        this.sheetNumber = "";
        this.tenantId = 0L;
        this.repOrderId = 0L;
        this.repOrderNumber = "";
        this.driverId = 0L;
        this.driverName = "";
        this.loadingDate = LocalDate.now(java.time.ZoneId.systemDefault());
        this.state = LoadingSheetState.DRAFT;
        this.createdBy = "";
        this.correlationId = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
    }

    // COPY-ON-WRITE constructor (Rule 15 compliant)
    public LoadingSheet(LoadingSheet source,
                        LoadingSheetState newState,
                        @Nullable List<LoadingSheetItem> newItems,
                        @Nullable List<LoadingSheetReturnItem> newReturns) {
        this.id = source.id;
        this.sheetNumber = source.sheetNumber;
        this.tenantId = source.tenantId;
        this.repOrderId = source.repOrderId;
        this.repOrderNumber = source.repOrderNumber;
        this.driverId = source.driverId;
        this.driverName = source.driverName;
        this.loadingDate = source.loadingDate;
        this.createdBy = source.createdBy;
        this.confirmedBy = source.confirmedBy;
        this.cancelledBy = source.cancelledBy;
        this.createdAt = source.createdAt;
        this.updatedAt = Instant.now();
        this.confirmedAt = source.confirmedAt;
        this.cancelledAt = source.cancelledAt;
        this.version = source.version;
        this.correlationId = source.correlationId;

        this.items = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>(source.items);
        this.returns = newReturns != null ? new ArrayList<>(newReturns) : new ArrayList<>(source.returns);
        this.state = newState;
    }

    // Full constructor for persistence adapter restoration
    public LoadingSheet(@Nullable Long id, String sheetNumber, Long tenantId, Long repOrderId, String repOrderNumber,
                        Long driverId, String driverName, LocalDate loadingDate, LoadingSheetState state,
                        String createdBy, @Nullable String confirmedBy, @Nullable String cancelledBy,
                        @Nullable Long version, String correlationId, Instant createdAt, Instant updatedAt,
                        @Nullable Instant confirmedAt, @Nullable Instant cancelledAt,
                        List<LoadingSheetItem> items, List<LoadingSheetReturnItem> returns) {
        this.id = id;
        this.sheetNumber = sheetNumber;
        this.tenantId = tenantId;
        this.repOrderId = repOrderId;
        this.repOrderNumber = repOrderNumber;
        this.driverId = driverId;
        this.driverName = driverName;
        this.loadingDate = loadingDate;
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
        this.items = new ArrayList<>(items);
        this.returns = new ArrayList<>(returns);
    }

    public static LoadingSheet create(String sheetNumber, Long tenantId, Long repOrderId, String repOrderNumber,
                                      Long driverId, String driverName, LocalDate loadingDate, String createdBy,
                                      List<LoadingSheetItem> items, List<LoadingSheetReturnItem> returns) {
        LoadingSheet ls = new LoadingSheet();
        ls.sheetNumber = sheetNumber;
        ls.tenantId = tenantId;
        ls.repOrderId = repOrderId;
        ls.repOrderNumber = repOrderNumber != null ? repOrderNumber : "";
        ls.driverId = driverId;
        ls.driverName = driverName != null ? driverName : "";
        ls.loadingDate = loadingDate;
        ls.state = LoadingSheetState.DRAFT;
        ls.createdBy = createdBy;
        ls.items = new ArrayList<>(items);
        ls.returns = new ArrayList<>(returns);
        ls.correlationId = sheetNumber;
        ls.createdAt = Instant.now();
        ls.updatedAt = Instant.now();
        return ls;
    }

    @Override
    public String getAggregateId() {
        return sheetNumber;
    }

    @Override
    public LoadingSheetState getWorkflowState() {
        return state;
    }

    @Override
    public WorkflowTransitionOutput<LoadingSheet> transitionTo(LoadingSheetState targetState, WorkflowContext context) {
        LoadingSheet copy = new LoadingSheet(this, targetState, this.items, this.returns);
        if (targetState == LoadingSheetState.CONFIRMED && copy.confirmedAt == null) {
            copy.confirmedAt = context.timestamp();
            copy.confirmedBy = String.valueOf(context.userId());
        }
        if (targetState == LoadingSheetState.CANCELLED && copy.cancelledAt == null) {
            copy.cancelledAt = context.timestamp();
            copy.cancelledBy = String.valueOf(context.userId());
        }

        DomainEventType eventType = switch (targetState) {
            case DRAFT -> DomainEventType.LOADING_SHEET_GENERATED;
            case CONFIRMED -> DomainEventType.LOADING_SHEET_CONFIRMED;
            case CANCELLED -> DomainEventType.LOADING_SHEET_CANCELLED;
        };

        LoadingSheetEvent event = new LoadingSheetEvent(
                this.sheetNumber,
                eventType,
                context.correlationId(),
                context.timestamp(),
                context.tenantId()
        );
        return new WorkflowTransitionOutput<>(copy, List.of(event));
    }

    // GETTERS ONLY (NO SETTERS — STRICT RULE 15)
    public @Nullable Long getId() { return id; }
    public String getSheetNumber() { return sheetNumber; }
    public Long getTenantId() { return tenantId; }
    public Long getRepOrderId() { return repOrderId; }
    public String getRepOrderNumber() { return repOrderNumber; }
    public Long getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public LocalDate getLoadingDate() { return loadingDate; }
    public LoadingSheetState getState() { return state; }
    public List<LoadingSheetItem> getItems() { return Collections.unmodifiableList(items); }
    public List<LoadingSheetReturnItem> getReturns() { return Collections.unmodifiableList(returns); }
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
