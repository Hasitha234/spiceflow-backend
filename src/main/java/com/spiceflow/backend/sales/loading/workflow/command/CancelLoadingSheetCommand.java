package com.spiceflow.backend.sales.loading.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.loading.domain.LoadingSheet;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetState;
import com.spiceflow.backend.sales.loading.workflow.LoadingSheetStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

public record CancelLoadingSheetCommand(@Nullable String comment) implements WorkflowCommand<LoadingSheet, LoadingSheetState> {

    public CancelLoadingSheetCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "CancelLoadingSheet";
    }

    @Override
    public LoadingSheetState getTargetState() {
        return LoadingSheetState.CANCELLED;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public void validate(LoadingSheet aggregate, WorkflowContext context) {
        if (!LoadingSheetStateMachine.canTransition(aggregate.getState(), getTargetState())) {
            throw new BusinessRuleViolationException(
                    "Cannot transition Loading Sheet from " + aggregate.getState() + " to " + getTargetState()
            );
        }
    }
}
