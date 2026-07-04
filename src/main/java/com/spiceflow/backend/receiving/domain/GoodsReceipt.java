package com.spiceflow.backend.receiving.domain;

import com.spiceflow.backend.events.DomainEventType;
import com.spiceflow.backend.workflow.WorkflowAggregate;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowTransitionOutput;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class GoodsReceipt implements WorkflowAggregate<GoodsReceipt, GoodsReceiptState> {

    private @Nullable Long id;
    private String receiptNumber;
    private Long tenantId;
    private @Nullable Long purchaseOrderId;
    private String poNumber;
    private Long supplierId;
    private Long warehouseId;
    private List<GoodsReceiptLine> lines = new ArrayList<>();
    private GoodsReceiptState state;
    private Instant receiptDate;
    private BigDecimal totalAcceptedValue;
    private BigDecimal totalDamagedValue;
    private String createdBy;
    private @Nullable String verifiedBy;
    private @Nullable String postedBy;
    private @Nullable Long version;
    private String correlationId;
    private Instant createdAt;
    private Instant updatedAt;
    private @Nullable Instant verifiedAt;
    private @Nullable Instant postedAt;

    protected GoodsReceipt() {
        // JPA only
        this.receiptNumber = "";
        this.tenantId = 0L;
        this.purchaseOrderId = null;
        this.poNumber = "";
        this.supplierId = 0L;
        this.warehouseId = 0L;
        this.state = GoodsReceiptState.DRAFT;
        this.receiptDate = Instant.EPOCH;
        this.totalAcceptedValue = BigDecimal.ZERO;
        this.totalDamagedValue = BigDecimal.ZERO;
        this.createdBy = "";
        this.verifiedBy = null;
        this.postedBy = null;
        this.correlationId = "";
        this.createdAt = Instant.EPOCH;
        this.updatedAt = Instant.EPOCH;
        this.verifiedAt = null;
        this.postedAt = null;
    }

    // COPY-ON-WRITE constructor (Rule 15 compliant)
    public GoodsReceipt(GoodsReceipt source,
                        GoodsReceiptState newState,
                        @Nullable List<GoodsReceiptLine> newLines) {

        this.id = source.id;
        this.receiptNumber = source.receiptNumber;
        this.tenantId = source.tenantId;
        this.purchaseOrderId = source.purchaseOrderId;
        this.poNumber = source.poNumber;
        this.supplierId = source.supplierId;
        this.warehouseId = source.warehouseId;
        this.receiptDate = source.receiptDate;
        this.createdBy = source.createdBy;
        this.verifiedBy = source.verifiedBy;
        this.postedBy = source.postedBy;
        this.createdAt = source.createdAt;
        this.updatedAt = Instant.now();
        this.verifiedAt = source.verifiedAt;
        this.postedAt = source.postedAt;

        // IMPORTANT: defensive copy (no shared references)
        this.lines = newLines != null
                ? new ArrayList<>(newLines)
                : new ArrayList<>(source.lines);

        this.totalAcceptedValue = this.lines.stream()
                .map(line -> line.getAcceptedQty().multiply(line.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalDamagedValue = this.lines.stream()
                .map(line -> line.getDamagedQty().multiply(line.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.state = newState;
        this.version = source.version;
        this.correlationId = source.correlationId;
    }

    // Full constructor for persistence adapter restoration
    public GoodsReceipt(@Nullable Long id, String receiptNumber, Long tenantId, @Nullable Long purchaseOrderId,
                        String poNumber, Long supplierId, Long warehouseId, GoodsReceiptState state,
                        Instant receiptDate, BigDecimal totalAcceptedValue, BigDecimal totalDamagedValue,
                        String createdBy, @Nullable String verifiedBy, @Nullable String postedBy,
                        @Nullable Long version, String correlationId, Instant createdAt, Instant updatedAt,
                        @Nullable Instant verifiedAt, @Nullable Instant postedAt, List<GoodsReceiptLine> lines) {
        this.id = id;
        this.receiptNumber = receiptNumber;
        this.tenantId = tenantId;
        this.purchaseOrderId = purchaseOrderId;
        this.poNumber = poNumber != null ? poNumber : "";
        this.supplierId = supplierId;
        this.warehouseId = warehouseId;
        this.state = state;
        this.receiptDate = receiptDate;
        this.totalAcceptedValue = totalAcceptedValue;
        this.totalDamagedValue = totalDamagedValue;
        this.createdBy = createdBy;
        this.verifiedBy = verifiedBy;
        this.postedBy = postedBy;
        this.version = version;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.verifiedAt = verifiedAt;
        this.postedAt = postedAt;
        this.lines = new ArrayList<>(lines);
    }

    public static GoodsReceipt create(String receiptNumber, Long tenantId, @Nullable Long purchaseOrderId,
                                      String poNumber, Long supplierId, Long warehouseId, String createdBy) {
        GoodsReceipt gr = new GoodsReceipt();
        gr.receiptNumber = receiptNumber;
        gr.tenantId = tenantId;
        gr.purchaseOrderId = purchaseOrderId;
        gr.poNumber = poNumber != null ? poNumber : "";
        gr.supplierId = supplierId;
        gr.warehouseId = warehouseId;
        gr.state = GoodsReceiptState.DRAFT;
        gr.receiptDate = Instant.now();
        gr.totalAcceptedValue = BigDecimal.ZERO;
        gr.totalDamagedValue = BigDecimal.ZERO;
        gr.createdBy = createdBy;
        gr.lines = new ArrayList<>();
        gr.correlationId = receiptNumber;
        gr.createdAt = Instant.now();
        gr.updatedAt = Instant.now();
        return gr;
    }

    @Override
    public String getAggregateId() {
        return receiptNumber;
    }

    @Override
    public GoodsReceiptState getWorkflowState() {
        return state;
    }

    @Override
    public WorkflowTransitionOutput<GoodsReceipt> transitionTo(GoodsReceiptState targetState, WorkflowContext context) {
        GoodsReceipt copy = new GoodsReceipt(this, targetState, this.lines);
        if (targetState == GoodsReceiptState.VERIFIED && copy.verifiedAt == null) {
            copy.verifiedAt = context.timestamp();
            copy.verifiedBy = String.valueOf(context.userId());
        }
        if (targetState == GoodsReceiptState.POSTED && copy.postedAt == null) {
            copy.postedAt = context.timestamp();
            copy.postedBy = String.valueOf(context.userId());
        }
        DomainEventType eventType = switch (targetState) {
            case DRAFT, INSPECTING -> DomainEventType.GOODS_RECEIPT_CREATED;
            case VERIFIED -> DomainEventType.GOODS_RECEIPT_VERIFIED;
            case POSTED -> DomainEventType.GOODS_RECEIPT_POSTED;
            case CANCELLED -> DomainEventType.GOODS_RECEIPT_CANCELLED;
        };
        GoodsReceiptEvent event = new GoodsReceiptEvent(
                this.receiptNumber,
                eventType,
                context.correlationId(),
                context.timestamp(),
                context.tenantId()
        );
        return new WorkflowTransitionOutput<>(copy, List.of(event));
    }

    // GETTERS ONLY (NO SETTERS — STRICT RULE 15)
    public @Nullable Long getId() { return id; }
    public String getReceiptNumber() { return receiptNumber; }
    public Long getTenantId() { return tenantId; }
    public @Nullable Long getPurchaseOrderId() { return purchaseOrderId; }
    public String getPoNumber() { return poNumber; }
    public Long getSupplierId() { return supplierId; }
    public Long getWarehouseId() { return warehouseId; }
    public List<GoodsReceiptLine> getLines() { return lines; }
    public GoodsReceiptState getState() { return state; }
    public Instant getReceiptDate() { return receiptDate; }
    public BigDecimal getTotalAcceptedValue() { return totalAcceptedValue; }
    public BigDecimal getTotalDamagedValue() { return totalDamagedValue; }
    public String getCreatedBy() { return createdBy; }
    public @Nullable String getVerifiedBy() { return verifiedBy; }
    public @Nullable String getPostedBy() { return postedBy; }
    public @Nullable Long getVersion() { return version; }
    public String getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public @Nullable Instant getVerifiedAt() { return verifiedAt; }
    public @Nullable Instant getPostedAt() { return postedAt; }
}
