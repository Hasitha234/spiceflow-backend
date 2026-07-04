package com.spiceflow.backend.inventory.transfer.workflow;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransfer;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferLine;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
import com.spiceflow.backend.inventory.transfer.workflow.command.ApproveTransferCommand;
import com.spiceflow.backend.inventory.transfer.workflow.command.ReceiveTransferCommand;
import com.spiceflow.backend.inventory.transfer.workflow.command.RequestTransferCommand;
import com.spiceflow.backend.inventory.transfer.workflow.command.ShipTransferCommand;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarehouseTransferWorkflowEngineTest {

    private WorkflowEngine engine;
    private WorkflowContext context;
    private WarehouseTransfer wt;

    @BeforeEach
    void setup() {
        engine = new WorkflowEngine();
        context = new WorkflowContext(
                1L,
                1L,
                "WT-TEST-001",
                Instant.now()
        );

        wt = WarehouseTransfer.create("WT-2026-0001", 1L, 10L, 20L, "system");
    }

    @Test
    void should_request_transfer_successfully_when_lines_valid() {
        WarehouseTransferLine line = new WarehouseTransferLine(
                100L, new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "LOT-X", new BigDecimal("15.00")
        );
        wt = new WarehouseTransfer(wt, WarehouseTransferState.DRAFT, List.of(line));

        WorkflowCommand<WarehouseTransfer, WarehouseTransferState> cmd = new RequestTransferCommand();
        WorkflowResult<WarehouseTransfer> result = engine.execute(cmd, wt, context);

        assertEquals(WarehouseTransferState.REQUESTED, result.updatedAggregate().getState());
        assertFalse(result.events().isEmpty());
        assertNotNull(result.auditEntry());
    }

    @Test
    void should_reject_request_when_lines_empty() {
        WorkflowCommand<WarehouseTransfer, WarehouseTransferState> cmd = new RequestTransferCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, wt, context)
        );
    }

    @Test
    void should_approve_and_ship_transfer_successfully() {
        WarehouseTransferLine line = new WarehouseTransferLine(
                100L, new BigDecimal("50"), new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO, "LOT-X", new BigDecimal("15.00")
        );
        wt = new WarehouseTransfer(wt, WarehouseTransferState.REQUESTED, List.of(line));

        WorkflowCommand<WarehouseTransfer, WarehouseTransferState> approveCmd = new ApproveTransferCommand();
        WorkflowResult<WarehouseTransfer> approveResult = engine.execute(approveCmd, wt, context);
        assertEquals(WarehouseTransferState.APPROVED, approveResult.updatedAggregate().getState());
        assertEquals("1", approveResult.updatedAggregate().getApprovedBy());

        WorkflowCommand<WarehouseTransfer, WarehouseTransferState> shipCmd = new ShipTransferCommand();
        WorkflowResult<WarehouseTransfer> shipResult = engine.execute(shipCmd, approveResult.updatedAggregate(), context);
        assertEquals(WarehouseTransferState.SHIPPED, shipResult.updatedAggregate().getState());
        assertEquals("1", shipResult.updatedAggregate().getShippedBy());
    }

    @Test
    void should_receive_transfer_successfully_when_quantities_match() {
        WarehouseTransferLine line = new WarehouseTransferLine(
                100L, new BigDecimal("50"), new BigDecimal("50"), new BigDecimal("48"), new BigDecimal("2"), "LOT-X", new BigDecimal("15.00")
        );
        wt = new WarehouseTransfer(wt, WarehouseTransferState.SHIPPED, List.of(line));

        WorkflowCommand<WarehouseTransfer, WarehouseTransferState> cmd = new ReceiveTransferCommand();
        WorkflowResult<WarehouseTransfer> result = engine.execute(cmd, wt, context);

        assertEquals(WarehouseTransferState.RECEIVED, result.updatedAggregate().getState());
        assertEquals("1", result.updatedAggregate().getReceivedBy());
        assertNotNull(result.updatedAggregate().getReceivedAt());
    }

    @Test
    void should_reject_receive_when_quantities_mismatch() {
        // received (45) + damaged (2) = 47 != shipped (50)
        WarehouseTransferLine line = new WarehouseTransferLine(
                100L, new BigDecimal("50"), new BigDecimal("50"), new BigDecimal("45"), new BigDecimal("2"), "LOT-X", new BigDecimal("15.00")
        );
        WarehouseTransfer shippedWt = new WarehouseTransfer(wt, WarehouseTransferState.SHIPPED, List.of(line));

        WorkflowCommand<WarehouseTransfer, WarehouseTransferState> cmd = new ReceiveTransferCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, shippedWt, context)
        );
    }

    @Test
    void should_return_new_aggregate_instance_not_mutate_original() {
        WarehouseTransferLine line = new WarehouseTransferLine(
                100L, new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "LOT-X", new BigDecimal("15.00")
        );
        wt = new WarehouseTransfer(wt, WarehouseTransferState.DRAFT, List.of(line));

        WorkflowCommand<WarehouseTransfer, WarehouseTransferState> cmd = new RequestTransferCommand();
        WarehouseTransfer beforeState = wt;
        WorkflowResult<WarehouseTransfer> result = engine.execute(cmd, wt, context);

        assertNotSame(beforeState, result.updatedAggregate());
        assertEquals(WarehouseTransferState.REQUESTED, result.updatedAggregate().getState());
        assertEquals(WarehouseTransferState.DRAFT, beforeState.getState());
    }
}
