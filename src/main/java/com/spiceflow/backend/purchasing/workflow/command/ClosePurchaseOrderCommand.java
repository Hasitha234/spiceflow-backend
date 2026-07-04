package com.spiceflow.backend.purchasing.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.purchasing.domain.PurchaseOrder;
import com.spiceflow.backend.purchasing.domain.PurchaseOrderState;
import com.spiceflow.backend.purchasing.workflow.PurchaseOrderStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

public record ClosePurchaseOrderCommand(@Nullable String comment) implements WorkflowCommand<PurchaseOrder, PurchaseOrderState> {

    public ClosePurchaseOrderCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "ClosePurchaseOrder";
    }

    @Override
    public PurchaseOrderState getTargetState() {
        return PurchaseOrderState.CLOSED;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public void validate(PurchaseOrder aggregate, WorkflowContext context) {
        if (!PurchaseOrderStateMachine.canTransition(aggregate.getState(), getTargetState())) {
            throw new BusinessRuleViolationException(
                    "Cannot transition Purchase Order from " + aggregate.getState() + " to " + getTargetState()
            );
        }
    }
}
