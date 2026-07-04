package com.spiceflow.backend.sales.delivery.workflow;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.delivery.domain.Delivery;
import com.spiceflow.backend.sales.delivery.domain.DeliveryShopRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryState;
import com.spiceflow.backend.sales.delivery.workflow.command.CancelDeliveryCommand;
import com.spiceflow.backend.sales.delivery.workflow.command.CompleteDeliveryCommand;
import com.spiceflow.backend.sales.delivery.workflow.command.DispatchDeliveryCommand;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeliveryWorkflowEngineTest {

    private WorkflowEngine engine;
    private WorkflowContext context;
    private Delivery delivery;

    @BeforeEach
    void setup() {
        engine = new WorkflowEngine();
        context = new WorkflowContext(1L, 10L, "DEL-TEST-001", Instant.now());

        delivery = Delivery.create("DEL-2026-0001", 10L, 200L, "LS-2026-0001",
                LocalDate.now(), "system", Collections.emptyList());
    }

    /** Helper: build a minimal DeliveryShopRecord for tests that require at least one shop. */
    private DeliveryShopRecord buildMinimalShopRecord() {
        return new DeliveryShopRecord(
                99L,
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO,
                List.of(),
                List.of(),
                List.of()
        );
    }

    @Test
    void should_dispatch_delivery_successfully() {
        WorkflowCommand<Delivery, DeliveryState> cmd = new DispatchDeliveryCommand("Route started");

        WorkflowResult<Delivery> result = engine.execute(cmd, delivery, context);

        assertNotNull(result);
        assertEquals(DeliveryState.DISPATCHED, result.updatedAggregate().getState());
        assertNotSame(delivery, result.updatedAggregate()); // Rule 15 — new instance
        assertFalse(result.events().isEmpty());
        assertEquals("DISPATCHED", result.auditEntry().getToState());
    }

    @Test
    void should_complete_delivery_with_shops() {
        Delivery withShop = new Delivery(delivery, DeliveryState.DISPATCHED, List.of(buildMinimalShopRecord()));

        WorkflowCommand<Delivery, DeliveryState> cmd = new CompleteDeliveryCommand("All shops visited");

        WorkflowResult<Delivery> result = engine.execute(cmd, withShop, context);

        assertEquals(DeliveryState.COMPLETED, result.updatedAggregate().getState());
        assertNotSame(withShop, result.updatedAggregate()); // Rule 15 — new instance
        assertEquals("COMPLETED", result.auditEntry().getToState());
    }

    @Test
    void should_fail_complete_delivery_with_no_shops() {
        Delivery dispatched = new Delivery(delivery, DeliveryState.DISPATCHED, List.of());
        WorkflowCommand<Delivery, DeliveryState> cmd = new CompleteDeliveryCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, dispatched, context)
        );
    }

    @Test
    void should_cancel_delivery_from_in_progress() {
        WorkflowCommand<Delivery, DeliveryState> cmd = new CancelDeliveryCommand("Customer not available");

        WorkflowResult<Delivery> result = engine.execute(cmd, delivery, context);

        assertEquals(DeliveryState.CANCELLED, result.updatedAggregate().getState());
        assertEquals("CANCELLED", result.auditEntry().getToState());
    }

    @Test
    void should_fail_invalid_transition_from_completed() {
        Delivery withShop = new Delivery(delivery, DeliveryState.COMPLETED, List.of(buildMinimalShopRecord()));
        WorkflowCommand<Delivery, DeliveryState> cmd = new CancelDeliveryCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, withShop, context)
        );
    }

    @Test
    void should_preserve_correlation_id_in_audit_entry() {
        WorkflowCommand<Delivery, DeliveryState> cmd = new DispatchDeliveryCommand();

        WorkflowResult<Delivery> result = engine.execute(cmd, delivery, context);

        assertEquals("DEL-TEST-001", result.auditEntry().getCorrelationId());
        assertEquals("DEL-2026-0001", result.auditEntry().getAggregateId());
    }
}
