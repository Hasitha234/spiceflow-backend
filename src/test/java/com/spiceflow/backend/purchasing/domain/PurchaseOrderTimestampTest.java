package com.spiceflow.backend.purchasing.domain;

import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowTransitionOutput;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Unit tests for PurchaseOrder domain aggregate.
 * Verifies Phase 3.1 timestamp tracking (submittedAt / receivedAt).
 */
class PurchaseOrderTimestampTest {

    private static final long SUPPLIER_ID = 100L;
    private static final long TENANT_ID = 10L;
    private static final String PO_NUMBER = "PO-2026-TS-0001";
    private static final String CREATED_BY = "buyer@spiceflow.com";

    @Test
    void submitted_at_should_be_null_on_creation() {
        PurchaseOrder po = PurchaseOrder.create(SUPPLIER_ID, PO_NUMBER, TENANT_ID, CREATED_BY);
        assertNull(po.getSubmittedAt(), "submittedAt must be null on a fresh DRAFT order");
    }

    @Test
    void received_at_should_be_null_on_creation() {
        PurchaseOrder po = PurchaseOrder.create(SUPPLIER_ID, PO_NUMBER, TENANT_ID, CREATED_BY);
        assertNull(po.getReceivedAt(), "receivedAt must be null on a fresh DRAFT order");
    }

    @Test
    void submitted_at_should_be_stamped_when_transitioning_to_submitted() {
        PurchaseOrder po = PurchaseOrder.create(SUPPLIER_ID, PO_NUMBER, TENANT_ID, CREATED_BY);
        Instant beforeTransition = Instant.now();

        WorkflowContext ctx = ctx();
        WorkflowTransitionOutput<PurchaseOrder> output = po.transitionTo(PurchaseOrderState.SUBMITTED, ctx);
        PurchaseOrder submitted = output.updatedAggregate();

        assertNotNull(submitted.getSubmittedAt(), "submittedAt must be stamped on SUBMITTED transition");
        // Timestamp must be at or after the transition point
        org.junit.jupiter.api.Assertions.assertFalse(
            submitted.getSubmittedAt().isBefore(beforeTransition),
            "submittedAt should not be before the transition was initiated"
        );
    }

    @Test
    void submitted_at_should_not_be_overwritten_on_subsequent_transitions() {
        PurchaseOrder po = PurchaseOrder.create(SUPPLIER_ID, PO_NUMBER, TENANT_ID, CREATED_BY);
        WorkflowContext submitCtx = ctx();
        PurchaseOrder submitted = po.transitionTo(PurchaseOrderState.SUBMITTED, submitCtx).updatedAggregate();
        Instant originalSubmittedAt = submitted.getSubmittedAt();

        WorkflowContext approveCtx = ctx();
        PurchaseOrder approved = submitted.transitionTo(PurchaseOrderState.APPROVED, approveCtx).updatedAggregate();

        assertEquals(originalSubmittedAt, approved.getSubmittedAt(),
            "submittedAt must be immutable after first stamp — subsequent transitions must not overwrite it");
    }

    @Test
    void received_at_should_be_stamped_when_transitioning_to_received() {
        PurchaseOrder po = buildOrderAtState(PurchaseOrderState.ORDERED);

        WorkflowContext ctx = ctx();
        PurchaseOrder received = po.transitionTo(PurchaseOrderState.RECEIVED, ctx).updatedAggregate();

        assertNotNull(received.getReceivedAt(), "receivedAt must be stamped on RECEIVED transition");
    }

    @Test
    void received_at_should_be_stamped_when_closing_without_full_receipt() {
        PurchaseOrder po = buildOrderAtState(PurchaseOrderState.PARTIALLY_RECEIVED);

        WorkflowContext ctx = ctx();
        PurchaseOrder closed = po.transitionTo(PurchaseOrderState.CLOSED, ctx).updatedAggregate();

        assertNotNull(closed.getReceivedAt(), "receivedAt must be stamped on CLOSED transition as a fallback receipt marker");
    }

    @Test
    void copy_on_write_constructor_should_preserve_timestamp_fields() {
        Instant submittedAt = Instant.parse("2026-06-01T09:00:00Z");
        PurchaseOrder original = new PurchaseOrder(
            null, PO_NUMBER, TENANT_ID, SUPPLIER_ID,
            PurchaseOrderState.SUBMITTED, Instant.now(), BigDecimal.ZERO,
            CREATED_BY, null, PO_NUMBER, Instant.now(), Instant.now(),
            submittedAt, null, List.of()
        );

        // Transition to APPROVED via copy-on-write
        PurchaseOrder copy = new PurchaseOrder(original, PurchaseOrderState.APPROVED, List.of());

        assertNotSame(original, copy);
        assertEquals(submittedAt, copy.getSubmittedAt(), "submittedAt must survive copy-on-write");
        assertNull(copy.getReceivedAt(), "receivedAt must remain null until RECEIVED/CLOSED");
    }

    private static WorkflowContext ctx() {
        return new WorkflowContext(1L, TENANT_ID, PO_NUMBER, Instant.now());
    }

    private PurchaseOrder buildOrderAtState(PurchaseOrderState targetState) {
        PurchaseOrder po = PurchaseOrder.create(SUPPLIER_ID, PO_NUMBER, TENANT_ID, CREATED_BY);
        if (targetState == PurchaseOrderState.DRAFT) return po;

        po = po.transitionTo(PurchaseOrderState.SUBMITTED, ctx()).updatedAggregate();
        if (targetState == PurchaseOrderState.SUBMITTED) return po;

        po = po.transitionTo(PurchaseOrderState.APPROVED, ctx()).updatedAggregate();
        if (targetState == PurchaseOrderState.APPROVED) return po;

        po = po.transitionTo(PurchaseOrderState.ORDERED, ctx()).updatedAggregate();
        if (targetState == PurchaseOrderState.ORDERED) return po;

        po = po.transitionTo(PurchaseOrderState.PARTIALLY_RECEIVED, ctx()).updatedAggregate();
        return po;
    }
}
