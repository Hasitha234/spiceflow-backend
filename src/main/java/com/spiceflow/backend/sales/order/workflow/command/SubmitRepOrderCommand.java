package com.spiceflow.backend.sales.order.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.order.domain.RepOrder;
import com.spiceflow.backend.sales.order.domain.RepOrderItem;
import com.spiceflow.backend.sales.order.domain.RepOrderShop;
import com.spiceflow.backend.sales.order.domain.RepOrderState;
import com.spiceflow.backend.sales.order.workflow.RepOrderStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

public record SubmitRepOrderCommand(@Nullable String comment) implements WorkflowCommand<RepOrder, RepOrderState> {

    public SubmitRepOrderCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "SubmitRepOrder";
    }

    @Override
    public RepOrderState getTargetState() {
        return RepOrderState.SUBMITTED;
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
            throw new BusinessRuleViolationException("Rep Order must have at least one shop order");
        }
        for (RepOrderShop shop : aggregate.getShops()) {
            if (shop.items().isEmpty()) {
                throw new BusinessRuleViolationException("Shop order for shop ID " + shop.shopId() + " must have at least one item");
            }
            for (RepOrderItem item : shop.items()) {
                if (item.quantity() <= 0) {
                    throw new BusinessRuleViolationException("Ordered quantity must be positive for product ID " + item.productId());
                }
                if (item.rate().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessRuleViolationException("Rate cannot be negative for product ID " + item.productId());
                }
            }
        }
    }
}
