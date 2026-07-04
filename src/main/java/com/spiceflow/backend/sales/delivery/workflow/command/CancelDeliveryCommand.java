package com.spiceflow.backend.sales.delivery.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.delivery.domain.Delivery;
import com.spiceflow.backend.sales.delivery.domain.DeliveryState;
import com.spiceflow.backend.sales.delivery.workflow.DeliveryStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

/**
 * Self-validating command intention to transition a Delivery to CANCELLED.
 * ADR-013: Validates FSM eligibility before the WorkflowEngine executes the transition.
 */
public record CancelDeliveryCommand(@Nullable String comment) implements WorkflowCommand<Delivery, DeliveryState> {

    public CancelDeliveryCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "CancelDelivery";
    }

    @Override
    public DeliveryState getTargetState() {
        return DeliveryState.CANCELLED;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public void validate(Delivery aggregate, WorkflowContext context) {
        if (!DeliveryStateMachine.canTransition(aggregate.getState(), getTargetState())) {
            throw new BusinessRuleViolationException(
                    "Cannot cancel Delivery from state: " + aggregate.getState()
            );
        }
    }
}
