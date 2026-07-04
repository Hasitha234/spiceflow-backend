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

public record ReceiveTransferCommand(@Nullable String comment) implements WorkflowCommand<WarehouseTransfer, WarehouseTransferState> {

    public ReceiveTransferCommand() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return "ReceiveTransfer";
    }

    @Override
    public WarehouseTransferState getTargetState() {
        return WarehouseTransferState.RECEIVED;
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
        for (WarehouseTransferLine line : aggregate.getLines()) {
            BigDecimal totalAccounted = line.getReceivedQty().add(line.getDamagedQty());
            if (totalAccounted.compareTo(line.getShippedQty()) != 0) {
                throw new BusinessRuleViolationException(
                        "Received quantity (" + line.getReceivedQty() + ") + Damaged quantity (" + line.getDamagedQty() +
                        ") must equal Shipped quantity (" + line.getShippedQty() + ") for product " + line.getProductId()
                );
            }
        }
    }
}
