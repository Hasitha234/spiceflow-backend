package com.spiceflow.backend.receiving.workflow;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.receiving.domain.GoodsReceipt;
import com.spiceflow.backend.receiving.domain.GoodsReceiptLine;
import com.spiceflow.backend.receiving.domain.GoodsReceiptState;
import com.spiceflow.backend.receiving.workflow.command.PostReceiptCommand;
import com.spiceflow.backend.receiving.workflow.command.SubmitInspectionCommand;
import com.spiceflow.backend.receiving.workflow.command.VerifyReceiptCommand;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoodsReceiptWorkflowEngineTest {

    private WorkflowEngine engine;
    private WorkflowContext context;
    private GoodsReceipt gr;

    @BeforeEach
    void setup() {
        engine = new WorkflowEngine();
        context = new WorkflowContext(
                1L,
                1L,
                "GR-TEST-001",
                Instant.now()
        );

        gr = GoodsReceipt.create("GR-2026-0001", 1L, 100L, "PO-2026-0001", 10L, 5L, "system");
    }

    @Test
    void should_submit_inspection_successfully() {
        WorkflowCommand<GoodsReceipt, GoodsReceiptState> cmd = new SubmitInspectionCommand();
        WorkflowResult<GoodsReceipt> result = engine.execute(cmd, gr, context);

        assertEquals(GoodsReceiptState.INSPECTING, result.updatedAggregate().getState());
        assertFalse(result.events().isEmpty());
        assertNotNull(result.auditEntry());
    }

    @Test
    void should_verify_receipt_successfully_when_quality_numbers_match() {
        GoodsReceiptLine line = new GoodsReceiptLine(
                1L, new BigDecimal("100"), new BigDecimal("100"),
                new BigDecimal("95"), new BigDecimal("5"), "LOT-A", LocalDate.now().plusDays(365), new BigDecimal("10.00")
        );
        gr = new GoodsReceipt(gr, GoodsReceiptState.INSPECTING, List.of(line));

        WorkflowCommand<GoodsReceipt, GoodsReceiptState> cmd = new VerifyReceiptCommand();
        WorkflowResult<GoodsReceipt> result = engine.execute(cmd, gr, context);

        assertEquals(GoodsReceiptState.VERIFIED, result.updatedAggregate().getState());
        assertEquals("1", result.updatedAggregate().getVerifiedBy());
        assertNotNull(result.updatedAggregate().getVerifiedAt());
    }

    @Test
    void should_reject_verification_when_quality_numbers_mismatch() {
        // accepted (90) + damaged (5) = 95 != received (100)
        GoodsReceiptLine line = new GoodsReceiptLine(
                1L, new BigDecimal("100"), new BigDecimal("100"),
                new BigDecimal("90"), new BigDecimal("5"), "LOT-A", LocalDate.now().plusDays(365), new BigDecimal("10.00")
        );
        GoodsReceipt inspectingGr = new GoodsReceipt(gr, GoodsReceiptState.INSPECTING, List.of(line));

        WorkflowCommand<GoodsReceipt, GoodsReceiptState> cmd = new VerifyReceiptCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, inspectingGr, context)
        );
    }

    @Test
    void should_reject_invalid_backward_transition() {
        gr = new GoodsReceipt(gr, GoodsReceiptState.VERIFIED, List.of());
        WorkflowCommand<GoodsReceipt, GoodsReceiptState> cmd = new SubmitInspectionCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, gr, context)
        );
    }

    @Test
    void should_propagate_correlation_id_through_audit() {
        WorkflowCommand<GoodsReceipt, GoodsReceiptState> cmd = new SubmitInspectionCommand();
        WorkflowResult<GoodsReceipt> result = engine.execute(cmd, gr, context);

        assertEquals("GR-TEST-001", result.auditEntry().getCorrelationId());
        assertEquals("GR-2026-0001", result.auditEntry().getAggregateId());
    }

    @Test
    void should_return_new_aggregate_instance_not_mutate_original() {
        WorkflowCommand<GoodsReceipt, GoodsReceiptState> cmd = new SubmitInspectionCommand();
        GoodsReceipt beforeState = gr;
        WorkflowResult<GoodsReceipt> result = engine.execute(cmd, gr, context);

        assertNotSame(beforeState, result.updatedAggregate());
        assertEquals(GoodsReceiptState.INSPECTING, result.updatedAggregate().getState());
        assertEquals(GoodsReceiptState.DRAFT, beforeState.getState());
    }
}
