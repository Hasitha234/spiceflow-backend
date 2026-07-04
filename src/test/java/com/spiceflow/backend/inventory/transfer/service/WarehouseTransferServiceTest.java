package com.spiceflow.backend.inventory.transfer.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.events.DomainEventPublisher;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.transfer.adapter.WarehouseTransferPersistenceAdapter;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransfer;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferLine;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
import com.spiceflow.backend.inventory.transfer.entity.WarehouseTransferEntity;
import com.spiceflow.backend.inventory.transfer.repository.WarehouseTransferRepository;
import com.spiceflow.backend.inventory.transfer.workflow.command.ReceiveTransferCommand;
import com.spiceflow.backend.inventory.transfer.workflow.command.ShipTransferCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseTransferServiceTest {

    @Mock private WarehouseTransferRepository repository;
    @Mock private WarehouseTransferPersistenceAdapter adapter;
    private WorkflowEngine engine = new WorkflowEngine();
    @Mock private InventoryLedgerService ledgerService;
    @Mock private DomainEventPublisher eventPublisher;
    @Mock private AuditService auditService;

    private WarehouseTransferService service;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        service = new WarehouseTransferService(
                repository, adapter, engine, ledgerService, eventPublisher, auditService
        );
        context = new WorkflowContext(1L, 10L, "WT-2026-0001", Instant.now());
    }

    @Test
    void should_create_transfer_successfully() {
        WarehouseTransfer wt = WarehouseTransfer.create("WT-2026-0001", 10L, 1L, 2L, "admin");
        WarehouseTransferEntity entity = new WarehouseTransferEntity();
        when(adapter.toEntity(wt)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(adapter.toDomain(entity)).thenReturn(wt);

        WarehouseTransfer created = service.createTransfer(wt);

        assertNotNull(created);
        assertEquals("WT-2026-0001", created.getTransferNumber());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void should_record_transfer_out_when_shipped() {
        WarehouseTransferLine line = new WarehouseTransferLine(
                100L, new BigDecimal("50"), new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO, "LOT-X", new BigDecimal("15.00")
        );
        WarehouseTransfer approvedWt = new WarehouseTransfer(
                WarehouseTransfer.create("WT-2026-0001", 10L, 1L, 2L, "admin"),
                WarehouseTransferState.APPROVED,
                List.of(line)
        );
        WarehouseTransferEntity entity = new WarehouseTransferEntity();

        when(repository.findByTransferNumberAndTenantId("WT-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toDomain(entity)).thenReturn(approvedWt);
        when(adapter.toEntity(any(WarehouseTransfer.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        service.executeCommand("WT-2026-0001", 10L, new ShipTransferCommand(), context);

        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(1L), eq(100L), eq(InventoryMovementType.TRANSFER_OUT),
                eq(new BigDecimal("-50")), eq(new BigDecimal("15.00")), eq("WT-2026-0001"),
                eq("LOT-X"), isNull(), any(Instant.class), eq("1")
        );
    }

    @Test
    void should_record_transfer_in_and_damaged_when_received() {
        WarehouseTransferLine line = new WarehouseTransferLine(
                100L, new BigDecimal("50"), new BigDecimal("50"), new BigDecimal("48"), new BigDecimal("2"), "LOT-X", new BigDecimal("15.00")
        );
        WarehouseTransfer shippedWt = new WarehouseTransfer(
                WarehouseTransfer.create("WT-2026-0001", 10L, 1L, 2L, "admin"),
                WarehouseTransferState.SHIPPED,
                List.of(line)
        );
        WarehouseTransferEntity entity = new WarehouseTransferEntity();

        when(repository.findByTransferNumberAndTenantId("WT-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toDomain(entity)).thenReturn(shippedWt);
        when(adapter.toEntity(any(WarehouseTransfer.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        service.executeCommand("WT-2026-0001", 10L, new ReceiveTransferCommand(), context);

        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(2L), eq(100L), eq(InventoryMovementType.TRANSFER_IN),
                eq(new BigDecimal("48")), eq(new BigDecimal("15.00")), eq("WT-2026-0001"),
                eq("LOT-X"), isNull(), any(Instant.class), eq("1")
        );

        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(2L), eq(100L), eq(InventoryMovementType.DAMAGED),
                eq(new BigDecimal("-2")), eq(new BigDecimal("15.00")), eq("WT-2026-0001"),
                eq("LOT-X"), isNull(), any(Instant.class), eq("1")
        );
    }
}
