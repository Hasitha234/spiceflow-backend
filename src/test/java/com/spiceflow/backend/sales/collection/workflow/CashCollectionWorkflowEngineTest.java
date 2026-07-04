package com.spiceflow.backend.sales.collection.workflow;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.collection.domain.CashCollection;
import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import com.spiceflow.backend.sales.collection.workflow.command.CancelCashCollectionCommand;
import com.spiceflow.backend.sales.collection.workflow.command.ConfirmCashCollectionCommand;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CashCollectionWorkflowEngineTest {

    private WorkflowEngine engine;
    private WorkflowContext context;
    private CashCollection collection;

    @BeforeEach
    void setup() {
        engine = new WorkflowEngine();
        context = new WorkflowContext(1L, 10L, "COL-TEST-001", Instant.now());

        collection = CashCollection.create("COL-2026-0001", 10L, 100L, 5L,
                LocalDate.now(ZoneOffset.UTC), BigDecimal.valueOf(5000),
                "CASH", null, null, null, "Payment received", "rep1");
    }

    @Test
    void should_confirm_collection_successfully() {
        WorkflowCommand<CashCollection, CashCollectionState> cmd = new ConfirmCashCollectionCommand("Payment verified");

        WorkflowResult<CashCollection> result = engine.execute(cmd, collection, context);

        assertNotNull(result);
        assertEquals(CashCollectionState.CONFIRMED, result.updatedAggregate().getState());
        assertNotSame(collection, result.updatedAggregate()); // Rule 15 copy-on-write
        assertFalse(result.events().isEmpty());
        assertEquals("CONFIRMED", result.auditEntry().getToState());
    }

    @Test
    void should_cancel_pending_collection_successfully() {
        WorkflowCommand<CashCollection, CashCollectionState> cmd = new CancelCashCollectionCommand("Entered in error");

        WorkflowResult<CashCollection> result = engine.execute(cmd, collection, context);

        assertEquals(CashCollectionState.CANCELLED, result.updatedAggregate().getState());
        assertEquals("CANCELLED", result.auditEntry().getToState());
    }

    @Test
    void should_cancel_confirmed_collection_successfully() {
        WorkflowResult<CashCollection> confirmedResult = engine.execute(
                new ConfirmCashCollectionCommand("Verified"), collection, context);

        WorkflowCommand<CashCollection, CashCollectionState> cancelCmd = new CancelCashCollectionCommand("Cheque bounced");
        WorkflowResult<CashCollection> cancelResult = engine.execute(cancelCmd, confirmedResult.updatedAggregate(), context);

        assertEquals(CashCollectionState.CANCELLED, cancelResult.updatedAggregate().getState());
    }

    @Test
    void should_fail_confirm_when_amount_is_zero() {
        CashCollection zeroAmount = CashCollection.create("COL-2026-0002", 10L, 100L, 5L,
                LocalDate.now(ZoneOffset.UTC), BigDecimal.ZERO,
                "CASH", null, null, null, "Zero payment", "rep1");

        WorkflowCommand<CashCollection, CashCollectionState> cmd = new ConfirmCashCollectionCommand("Confirm zero");

        assertThrows(BusinessRuleViolationException.class, () -> engine.execute(cmd, zeroAmount, context));
    }

    @Test
    void should_fail_confirm_when_already_confirmed() {
        WorkflowResult<CashCollection> confirmedResult = engine.execute(
                new ConfirmCashCollectionCommand("Verified"), collection, context);

        WorkflowCommand<CashCollection, CashCollectionState> secondConfirm = new ConfirmCashCollectionCommand("Again");

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(secondConfirm, confirmedResult.updatedAggregate(), context));
    }
}
