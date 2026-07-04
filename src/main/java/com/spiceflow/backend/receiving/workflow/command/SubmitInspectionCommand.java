package com.spiceflow.backend.receiving.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.receiving.domain.GoodsReceipt;
import com.spiceflow.backend.receiving.domain.GoodsReceiptState;
import com.spiceflow.backend.receiving.workflow.GoodsReceiptStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

public record SubmitInspectionCommand(@Nullable String comment) implements WorkflowCommand<GoodsReceipt, GoodsReceiptState> {

    public SubmitInspectionCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "SubmitInspection";
    }

    @Override
    public GoodsReceiptState getTargetState() {
        return GoodsReceiptState.INSPECTING;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public void validate(GoodsReceipt aggregate, WorkflowContext context) {
        if (!GoodsReceiptStateMachine.canTransition(aggregate.getState(), getTargetState())) {
            throw new BusinessRuleViolationException(
                    "Cannot transition Goods Receipt from " + aggregate.getState() + " to " + getTargetState()
            );
        }
    }
}
