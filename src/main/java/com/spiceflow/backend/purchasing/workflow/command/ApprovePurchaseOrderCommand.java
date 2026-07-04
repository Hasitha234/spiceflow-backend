package com.spiceflow.backend.purchasing.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.purchasing.domain.PurchaseOrder;
import com.spiceflow.backend.purchasing.domain.PurchaseOrderState;
import com.spiceflow.backend.purchasing.workflow.PurchaseOrderStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

public record ApprovePurchaseOrderCommand(@Nullable String comment) implements WorkflowCommand<PurchaseOrder, PurchaseOrderState> {

    public ApprovePurchaseOrderCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "ApprovePurchaseOrder";
    }

    @Override
    public PurchaseOrderState getTargetState() {
        return PurchaseOrderState.APPROVED;
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
