package com.spiceflow.backend.sales.order.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.order.domain.RepOrder;
import com.spiceflow.backend.sales.order.domain.RepOrderState;
import com.spiceflow.backend.sales.order.workflow.RepOrderStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

public record LoadRepOrderCommand(@Nullable String comment) implements WorkflowCommand<RepOrder, RepOrderState> {

    public LoadRepOrderCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "LoadRepOrder";
    }

    @Override
    public RepOrderState getTargetState() {
        return RepOrderState.LOADED;
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
        if (aggregate.getShops().isEmpty()) {
            throw new BusinessRuleViolationException("Cannot load Rep Order without any shop orders");
        }
    }
}
