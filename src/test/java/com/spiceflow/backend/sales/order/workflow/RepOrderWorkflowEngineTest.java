package com.spiceflow.backend.sales.order.workflow;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.order.domain.RepOrder;
import com.spiceflow.backend.sales.order.domain.RepOrderItem;
import com.spiceflow.backend.sales.order.domain.RepOrderShop;
import com.spiceflow.backend.sales.order.domain.RepOrderState;
import com.spiceflow.backend.sales.order.domain.ShopReturnItem;
import com.spiceflow.backend.sales.order.workflow.command.ApproveRepOrderCommand;
import com.spiceflow.backend.sales.order.workflow.command.DeliverRepOrderCommand;
import com.spiceflow.backend.sales.order.workflow.command.LoadRepOrderCommand;
import com.spiceflow.backend.sales.order.workflow.command.SubmitRepOrderCommand;
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

class RepOrderWorkflowEngineTest {

    private WorkflowEngine engine;
    private WorkflowContext context;
    private RepOrder repOrder;

    @BeforeEach
    void setup() {
        engine = new WorkflowEngine();
        context = new WorkflowContext(
                1L,
                1L,
                "RO-TEST-001",
                Instant.now()
        );

        repOrder = RepOrder.create("RO-2026-0001", 1L, 10L, LocalDate.now(), "Galle Route", "system");
    }

    @Test
    void should_submit_rep_order_successfully_when_shops_and_items_valid() {
        RepOrderItem item = new RepOrderItem(
                100L, 10, "PACK", new BigDecimal("150.00"), false, 1
        );
        RepOrderShop shop = new RepOrderShop(
                50L, List.of(item), Collections.emptyList()
        );
        repOrder = new RepOrder(repOrder, RepOrderState.DRAFT, List.of(shop));

        WorkflowCommand<RepOrder, RepOrderState> cmd = new SubmitRepOrderCommand();
        WorkflowResult<RepOrder> result = engine.execute(cmd, repOrder, context);

        assertEquals(RepOrderState.SUBMITTED, result.updatedAggregate().getState());
        assertFalse(result.events().isEmpty());
        assertNotNull(result.auditEntry());
        assertEquals(new BigDecimal("1500.00"), result.updatedAggregate().getTotalGrossAmount());
    }

    @Test
    void should_reject_submit_when_shops_empty() {
        WorkflowCommand<RepOrder, RepOrderState> cmd = new SubmitRepOrderCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, repOrder, context)
        );
    }

    @Test
    void should_reject_submit_when_shop_items_empty() {
        RepOrderShop shop = new RepOrderShop(50L, Collections.emptyList(), Collections.emptyList());
        repOrder = new RepOrder(repOrder, RepOrderState.DRAFT, List.of(shop));

        WorkflowCommand<RepOrder, RepOrderState> cmd = new SubmitRepOrderCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, repOrder, context)
        );
    }

    @Test
    void should_approve_load_and_deliver_rep_order_successfully() {
        RepOrderItem item = new RepOrderItem(
                100L, 10, "PACK", new BigDecimal("150.00"), false, 1
        );
        ShopReturnItem returnItem = new ShopReturnItem(
                200L, 2, "PACK", new BigDecimal("100.00"), "EXPIRED"
        );
        RepOrderShop shop = new RepOrderShop(
                50L, List.of(item), List.of(returnItem)
        );
        repOrder = new RepOrder(repOrder, RepOrderState.SUBMITTED, List.of(shop));

        WorkflowCommand<RepOrder, RepOrderState> approveCmd = new ApproveRepOrderCommand();
        WorkflowResult<RepOrder> approveResult = engine.execute(approveCmd, repOrder, context);
        assertEquals(RepOrderState.APPROVED, approveResult.updatedAggregate().getState());
        assertEquals("1", approveResult.updatedAggregate().getApprovedBy());

        WorkflowCommand<RepOrder, RepOrderState> loadCmd = new LoadRepOrderCommand();
        WorkflowResult<RepOrder> loadResult = engine.execute(loadCmd, approveResult.updatedAggregate(), context);
        assertEquals(RepOrderState.LOADED, loadResult.updatedAggregate().getState());
        assertEquals("1", loadResult.updatedAggregate().getLoadedBy());

        WorkflowCommand<RepOrder, RepOrderState> deliverCmd = new DeliverRepOrderCommand();
        WorkflowResult<RepOrder> deliverResult = engine.execute(deliverCmd, loadResult.updatedAggregate(), context);
        assertEquals(RepOrderState.DELIVERED, deliverResult.updatedAggregate().getState());
        assertEquals("1", deliverResult.updatedAggregate().getDeliveredBy());
        assertEquals(new BigDecimal("1400.00"), deliverResult.updatedAggregate().getNetAmount());
    }

    @Test
    void should_return_new_aggregate_instance_not_mutate_original() {
        RepOrderItem item = new RepOrderItem(
                100L, 10, "PACK", new BigDecimal("150.00"), false, 1
        );
        RepOrderShop shop = new RepOrderShop(50L, List.of(item), Collections.emptyList());
        repOrder = new RepOrder(repOrder, RepOrderState.DRAFT, List.of(shop));

        WorkflowCommand<RepOrder, RepOrderState> cmd = new SubmitRepOrderCommand();
        RepOrder beforeState = repOrder;
        WorkflowResult<RepOrder> result = engine.execute(cmd, repOrder, context);

        assertNotSame(beforeState, result.updatedAggregate());
        assertEquals(RepOrderState.SUBMITTED, result.updatedAggregate().getState());
        assertEquals(RepOrderState.DRAFT, beforeState.getState());
    }
}
