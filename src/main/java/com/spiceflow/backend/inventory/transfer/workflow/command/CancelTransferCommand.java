package com.spiceflow.backend.inventory.transfer.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransfer;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
import com.spiceflow.backend.inventory.transfer.workflow.WarehouseTransferStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import org.jspecify.annotations.Nullable;

public record CancelTransferCommand(@Nullable String comment) implements WorkflowCommand<WarehouseTransfer, WarehouseTransferState> {

    public CancelTransferCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "CancelTransfer";
    }

    @Override
    public WarehouseTransferState getTargetState() {
        return WarehouseTransferState.CANCELLED;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }

    @Override
    public void validate(WarehouseTransfer aggregate, WorkflowContext context) {
        if (!WarehouseTransferStateMachine.canTransition(aggregate.getState(), getTargetState())) {
            throw new BusinessRuleViolationException(
                    "Cannot transition Warehouse Transfer from " + aggregate.getState() + " to " + getTargetState()
            );
        }
    }
}
