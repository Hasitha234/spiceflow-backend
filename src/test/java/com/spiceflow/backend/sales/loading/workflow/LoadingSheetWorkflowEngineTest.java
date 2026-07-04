package com.spiceflow.backend.sales.loading.workflow;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.loading.domain.LoadingSheet;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetItem;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetReturnItem;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetState;
import com.spiceflow.backend.sales.loading.workflow.command.CancelLoadingSheetCommand;
import com.spiceflow.backend.sales.loading.workflow.command.ConfirmLoadingSheetCommand;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
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

class LoadingSheetWorkflowEngineTest {

    private WorkflowEngine engine;
    private WorkflowContext context;
    private LoadingSheet loadingSheet;

    @BeforeEach
    void setup() {
        engine = new WorkflowEngine();
        context = new WorkflowContext(
                1L,
                1L,
                "LS-TEST-001",
                Instant.now()
        );

        loadingSheet = LoadingSheet.create("LS-2026-0001", 1L, 100L, "RO-2026-0001", 5L, "John Driver", LocalDate.now(), "system", Collections.emptyList(), Collections.emptyList());
    }

    @Test
    void should_confirm_loading_sheet_successfully_when_items_exist() {
        LoadingSheetItem item = new LoadingSheetItem(10L, 50, "PACK");
        loadingSheet = new LoadingSheet(loadingSheet, LoadingSheetState.DRAFT, List.of(item), Collections.emptyList());

        WorkflowCommand<LoadingSheet, LoadingSheetState> cmd = new ConfirmLoadingSheetCommand("Confirmed loading");

        WorkflowResult<LoadingSheet> result = engine.execute(cmd, loadingSheet, context);

        assertNotNull(result);
        assertEquals(LoadingSheetState.CONFIRMED, result.updatedAggregate().getState());
        assertNotSame(loadingSheet, result.updatedAggregate()); // Rule 15 immutability
        assertFalse(result.events().isEmpty());
        assertEquals("CONFIRMED", result.auditEntry().getToState());
    }

    @Test
    void should_fail_to_confirm_when_no_items_exist() {
        WorkflowCommand<LoadingSheet, LoadingSheetState> cmd = new ConfirmLoadingSheetCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, loadingSheet, context)
        );
    }

    @Test
    void should_cancel_loading_sheet_successfully() {
        WorkflowCommand<LoadingSheet, LoadingSheetState> cmd = new CancelLoadingSheetCommand("Driver unavailable");

        WorkflowResult<LoadingSheet> result = engine.execute(cmd, loadingSheet, context);

        assertEquals(LoadingSheetState.CANCELLED, result.updatedAggregate().getState());
        assertEquals("CANCELLED", result.auditEntry().getToState());
    }

    @Test
    void should_fail_invalid_transition_from_confirmed_to_cancelled() {
        LoadingSheetItem item = new LoadingSheetItem(10L, 50, "PACK");
        loadingSheet = new LoadingSheet(loadingSheet, LoadingSheetState.CONFIRMED, List.of(item), Collections.emptyList());

        WorkflowCommand<LoadingSheet, LoadingSheetState> cmd = new CancelLoadingSheetCommand();

        assertThrows(BusinessRuleViolationException.class, () ->
                engine.execute(cmd, loadingSheet, context)
        );
    }
}
