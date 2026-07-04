package com.spiceflow.backend.sales.collection.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.collection.domain.CashCollection;
import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import com.spiceflow.backend.sales.collection.workflow.CashCollectionStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

/**
 * Self-validating command intention to transition a CashCollection to CANCELLED.
 * ADR-013: Validates FSM eligibility before execution.
 */
public record CancelCashCollectionCommand(@Nullable String comment) implements WorkflowCommand<CashCollection, CashCollectionState> {

    public CancelCashCollectionCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "CancelCashCollection";
    }

    @Override
    public CashCollectionState getTargetState() {
        return CashCollectionState.CANCELLED;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public void validate(CashCollection aggregate, WorkflowContext context) {
        if (!CashCollectionStateMachine.canTransition(aggregate.getState(), getTargetState())) {
            throw new BusinessRuleViolationException(
                    "Cannot cancel Cash Collection from state: " + aggregate.getState()
            );
        }
    }
}
