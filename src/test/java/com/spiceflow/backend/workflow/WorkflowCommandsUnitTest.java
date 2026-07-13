package com.spiceflow.backend.workflow;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransfer;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
import com.spiceflow.backend.inventory.transfer.workflow.command.CancelTransferCommand;
import com.spiceflow.backend.purchasing.domain.PurchaseOrder;
import com.spiceflow.backend.purchasing.domain.PurchaseOrderState;
import com.spiceflow.backend.purchasing.workflow.command.ApprovePurchaseOrderCommand;
import com.spiceflow.backend.receiving.domain.GoodsReceipt;
import com.spiceflow.backend.receiving.domain.GoodsReceiptState;
import com.spiceflow.backend.receiving.workflow.command.CancelReceiptCommand;
import com.spiceflow.backend.receiving.workflow.command.PostReceiptCommand;
import com.spiceflow.backend.sales.order.domain.RepOrder;
import com.spiceflow.backend.sales.order.domain.RepOrderState;
import com.spiceflow.backend.sales.order.workflow.command.CancelRepOrderCommand;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkflowCommandsUnitTest {

    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        context = WorkflowContext.of(100L, 1L, "TEST-CORR-1");
    }

    @Test
    @DisplayName("CancelReceiptCommand and PostReceiptCommand validation and properties")
    void testReceiptCommands() {
        GoodsReceipt grDraft = GoodsReceipt.create("GR-001", 1L, 10L, "PO-001", 20L, 30L, "user1");

        CancelReceiptCommand cancelCmd = new CancelReceiptCommand("Cancelling draft");
        assertThat(cancelCmd.getCommandName()).isEqualTo("CancelReceipt");
        assertThat(cancelCmd.getTargetState()).isEqualTo(GoodsReceiptState.CANCELLED);
        assertThat(cancelCmd.getComment()).isEqualTo("Cancelling draft");
        cancelCmd.validate(grDraft, context);

        CancelReceiptCommand cancelNoArg = new CancelReceiptCommand();
        assertThat(cancelNoArg.getComment()).isNull();

        PostReceiptCommand postCmd = new PostReceiptCommand("Posting verified receipt");
        assertThat(postCmd.getCommandName()).isEqualTo("PostReceipt");
        assertThat(postCmd.getTargetState()).isEqualTo(GoodsReceiptState.POSTED);
        assertThat(postCmd.getComment()).isEqualTo("Posting verified receipt");

        // Transition draft -> verified so we can test posting validation
        WorkflowTransitionOutput<GoodsReceipt> verifiedOutput = grDraft.transitionTo(GoodsReceiptState.VERIFIED, context);
        GoodsReceipt grVerified = verifiedOutput.updatedAggregate();
        postCmd.validate(grVerified, context);

        // Cannot post from draft directly
        assertThatThrownBy(() -> postCmd.validate(grDraft, context))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot transition Goods Receipt from DRAFT to POSTED");
    }

    @Test
    @DisplayName("CancelRepOrderCommand validation and properties")
    void testCancelRepOrderCommand() {
        RepOrder order = RepOrder.create("RO-001", 1L, 10L, LocalDate.now(), "Route A", "user1");

        CancelRepOrderCommand cmd = new CancelRepOrderCommand("Cancelling order");
        assertThat(cmd.getCommandName()).isEqualTo("CancelRepOrder");
        assertThat(cmd.getTargetState()).isEqualTo(RepOrderState.CANCELLED);
        assertThat(cmd.getComment()).isEqualTo("Cancelling order");
        cmd.validate(order, context);

        CancelRepOrderCommand noArg = new CancelRepOrderCommand();
        assertThat(noArg.getComment()).isNull();

        WorkflowTransitionOutput<RepOrder> cancelledOutput = order.transitionTo(RepOrderState.CANCELLED, context);
        RepOrder cancelledOrder = cancelledOutput.updatedAggregate();
        assertThatThrownBy(() -> cmd.validate(cancelledOrder, context))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot transition Rep Order from CANCELLED to CANCELLED");
    }

    @Test
    @DisplayName("CancelTransferCommand validation and properties")
    void testCancelTransferCommand() {
        WarehouseTransfer transfer = WarehouseTransfer.create("WT-001", 1L, 10L, 20L, "user1");

        CancelTransferCommand cmd = new CancelTransferCommand("Cancelling transfer");
        assertThat(cmd.getCommandName()).isEqualTo("CancelTransfer");
        assertThat(cmd.getTargetState()).isEqualTo(WarehouseTransferState.CANCELLED);
        assertThat(cmd.getComment()).isEqualTo("Cancelling transfer");
        cmd.validate(transfer, context);

        CancelTransferCommand noArg = new CancelTransferCommand();
        assertThat(noArg.getComment()).isNull();

        WorkflowTransitionOutput<WarehouseTransfer> cancelledOutput = transfer.transitionTo(WarehouseTransferState.CANCELLED, context);
        WarehouseTransfer cancelledTransfer = cancelledOutput.updatedAggregate();
        assertThatThrownBy(() -> cmd.validate(cancelledTransfer, context))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot transition Warehouse Transfer from CANCELLED to CANCELLED");
    }

    @Test
    @DisplayName("ApprovePurchaseOrderCommand validation and properties")
    void testApprovePurchaseOrderCommand() {
        PurchaseOrder poDraft = PurchaseOrder.create(20L, "PO-001", 1L, "user1");

        ApprovePurchaseOrderCommand cmd = new ApprovePurchaseOrderCommand("Approved");
        assertThat(cmd.getCommandName()).isEqualTo("ApprovePurchaseOrder");
        assertThat(cmd.getTargetState()).isEqualTo(PurchaseOrderState.APPROVED);
        assertThat(cmd.getComment()).isEqualTo("Approved");

        WorkflowTransitionOutput<PurchaseOrder> submittedOutput = poDraft.transitionTo(PurchaseOrderState.SUBMITTED, context);
        PurchaseOrder poSubmitted = submittedOutput.updatedAggregate();
        cmd.validate(poSubmitted, context);

        ApprovePurchaseOrderCommand noArg = new ApprovePurchaseOrderCommand();
        assertThat(noArg.getComment()).isNull();

        // Cannot approve directly from draft
        assertThatThrownBy(() -> cmd.validate(poDraft, context))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot transition Purchase Order from DRAFT to APPROVED");
    }
}
