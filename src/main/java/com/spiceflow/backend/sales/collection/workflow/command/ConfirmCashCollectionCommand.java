package com.spiceflow.backend.sales.collection.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.collection.domain.CashCollection;
import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import com.spiceflow.backend.sales.collection.workflow.CashCollectionStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Self-validating command intention to transition a CashCollection to CONFIRMED.
 * ADR-013: Validates FSM eligibility and positive amount before execution.
 */
public record ConfirmCashCollectionCommand(@Nullable String comment) implements WorkflowCommand<CashCollection, CashCollectionState> {

    public ConfirmCashCollectionCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "ConfirmCashCollection";
    }

    @Override
    public CashCollectionState getTargetState() {
        return CashCollectionState.CONFIRMED;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public void validate(CashCollection aggregate, WorkflowContext context) {
        if (!CashCollectionStateMachine.canTransition(aggregate.getState(), getTargetState())) {
            throw new BusinessRuleViolationException(
                    "Cannot confirm Cash Collection from state: " + aggregate.getState()
            );
        }
        if (aggregate.getAmount() == null || aggregate.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException(
                    "Cash collection amount must be greater than zero"
            );
        }
    }
}
