package com.spiceflow.backend.sales.delivery.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.delivery.domain.Delivery;
import com.spiceflow.backend.sales.delivery.domain.DeliveryState;
import com.spiceflow.backend.sales.delivery.workflow.DeliveryStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

/**
 * Self-validating command intention to transition a Delivery to COMPLETED.
 * ADR-013: Validates FSM eligibility and shop delivery completeness before execution.
 */
public record CompleteDeliveryCommand(@Nullable String comment) implements WorkflowCommand<Delivery, DeliveryState> {

    public CompleteDeliveryCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "CompleteDelivery";
    }

    @Override
    public DeliveryState getTargetState() {
        return DeliveryState.COMPLETED;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public void validate(Delivery aggregate, WorkflowContext context) {
        if (!DeliveryStateMachine.canTransition(aggregate.getState(), getTargetState())) {
            throw new BusinessRuleViolationException(
                    "Cannot complete Delivery from state: " + aggregate.getState()
            );
        }
        if (aggregate.getShops().isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Cannot complete a Delivery with no recorded shop deliveries"
            );
        }
    }
}
