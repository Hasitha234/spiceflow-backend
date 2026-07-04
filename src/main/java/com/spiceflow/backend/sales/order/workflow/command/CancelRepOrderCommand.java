package com.spiceflow.backend.sales.order.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.order.domain.RepOrder;
import com.spiceflow.backend.sales.order.domain.RepOrderState;
import com.spiceflow.backend.sales.order.workflow.RepOrderStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

public record CancelRepOrderCommand(@Nullable String comment) implements WorkflowCommand<RepOrder, RepOrderState> {

    public CancelRepOrderCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "CancelRepOrder";
    }

    @Override
    public RepOrderState getTargetState() {
        return RepOrderState.CANCELLED;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public void validate(RepOrder aggregate, WorkflowContext context) {
        if (!RepOrderStateMachine.canTransition(aggregate.getState(), getTargetState())) {
            throw new BusinessRuleViolationException(
                    "Cannot transition Rep Order from " + aggregate.getState() + " to " + getTargetState()
            );
        }
    }
}
