package com.spiceflow.backend.inventory.transfer.workflow.command;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransfer;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferLine;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
import com.spiceflow.backend.inventory.transfer.workflow.WarehouseTransferStateMachine;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

public record RequestTransferCommand(@Nullable String comment) implements WorkflowCommand<WarehouseTransfer, WarehouseTransferState> {

    public RequestTransferCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "RequestTransfer";
    }

    @Override
    public WarehouseTransferState getTargetState() {
        return WarehouseTransferState.REQUESTED;
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
        if (aggregate.getLines().isEmpty()) {
            throw new BusinessRuleViolationException("Warehouse Transfer must have at least one line item");
        }
        for (WarehouseTransferLine line : aggregate.getLines()) {
            if (line.getRequestedQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleViolationException("Requested quantity must be positive for product " + line.getProductId());
            }
        }
    }
}
