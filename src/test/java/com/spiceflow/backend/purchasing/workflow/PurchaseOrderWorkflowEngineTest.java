package com.spiceflow.backend.purchasing.workflow;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.purchasing.domain.PurchaseOrder;
import com.spiceflow.backend.purchasing.domain.PurchaseOrderState;
import com.spiceflow.backend.purchasing.workflow.command.ClosePurchaseOrderCommand;
import com.spiceflow.backend.purchasing.workflow.command.SubmitPurchaseOrderCommand;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseOrderWorkflowEngineTest {

    private WorkflowEngine engine;
    private WorkflowContext context;
    private PurchaseOrder po;

    @BeforeEach
    void setup() {
        engine = new WorkflowEngine();
        context = new WorkflowContext(
                1L,
                1L,
                "PO-TEST-001",
                Instant.now()
        );

        po = PurchaseOrder.create(100L, "PO-2026-0001");
    }

    // 1. VALID TRANSITION: DRAFT → SUBMITTED
    @Test
    void should_submit_purchase_order_successfully() {

        WorkflowCommand<PurchaseOrder, PurchaseOrderState> cmd = new SubmitPurchaseOrderCommand();

        WorkflowResult<PurchaseOrder> result = engine.execute(cmd, po, context);

        assertEquals(PurchaseOrderState.SUBMITTED,
                result.updatedAggregate().getState());

        assertFalse(result.events().isEmpty());
        assertNotNull(result.auditEntry());
    }

    // 2. INVALID TRANSITION: SUBMITTED → DRAFT (must fail)
    @Test
    void should_reject_invalid_backward_transition() {

        po = new PurchaseOrder(po, PurchaseOrderState.SUBMITTED, List.of());

        WorkflowCommand<PurchaseOrder, PurchaseOrderState> cmd = new SubmitPurchaseOrderCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, po, context)
        );
    }

    // 3. FSM VALIDATION: APPROVED → CLOSED (invalid jump)
    @Test
    void should_prevent_skipping_states() {

        po = new PurchaseOrder(po, PurchaseOrderState.APPROVED, List.of());

        WorkflowCommand<PurchaseOrder, PurchaseOrderState> cmd = new ClosePurchaseOrderCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, po, context)
        );
    }

    // 4. CORRELATION ID PROPAGATION TEST
    @Test
    void should_propagate_correlation_id_through_audit() {

        WorkflowCommand<PurchaseOrder, PurchaseOrderState> cmd = new SubmitPurchaseOrderCommand();

        WorkflowResult<PurchaseOrder> result = engine.execute(cmd, po, context);

        assertEquals("PO-TEST-001",
                result.auditEntry().getCorrelationId());
        assertEquals("PO-2026-0001",
                result.auditEntry().getAggregateId());
    }

    // 5. IMMUTABILITY (RULE 15 ENFORCEMENT)
    @Test
    void should_return_new_aggregate_instance_not_mutate_original() {

        WorkflowCommand<PurchaseOrder, PurchaseOrderState> cmd = new SubmitPurchaseOrderCommand();

        PurchaseOrder beforeState = po;

        WorkflowResult<PurchaseOrder> result = engine.execute(cmd, po, context);

        assertNotSame(beforeState, result.updatedAggregate());
        assertEquals(PurchaseOrderState.SUBMITTED,
                result.updatedAggregate().getState());

        // original MUST remain unchanged
        assertEquals(PurchaseOrderState.DRAFT, beforeState.getState());
    }
}
