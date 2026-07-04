package com.spiceflow.backend.receiving.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.receiving.domain.GoodsReceipt;
import com.spiceflow.backend.receiving.domain.GoodsReceiptLine;
import com.spiceflow.backend.receiving.domain.GoodsReceiptState;
import com.spiceflow.backend.receiving.workflow.GoodsReceiptStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

public record VerifyReceiptCommand(@Nullable String comment) implements WorkflowCommand<GoodsReceipt, GoodsReceiptState> {

    public VerifyReceiptCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "VerifyReceipt";
    }

    @Override
    public GoodsReceiptState getTargetState() {
        return GoodsReceiptState.VERIFIED;
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
        for (GoodsReceiptLine line : aggregate.getLines()) {
            if (line.getAcceptedQty().add(line.getDamagedQty()).compareTo(line.getReceivedQty()) != 0) {
                throw new BusinessRuleViolationException(
                        "Quality inspection mismatch for product ID " + line.getProductId() +
                        ": accepted (" + line.getAcceptedQty() + ") + damaged (" + line.getDamagedQty() +
                        ") must equal received (" + line.getReceivedQty() + ")"
                );
            }
        }
    }
}
