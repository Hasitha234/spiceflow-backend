package com.spiceflow.backend.sales.loading.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.events.DomainEventPublisher;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.sales.loading.adapter.LoadingSheetPersistenceAdapter;
import com.spiceflow.backend.sales.loading.domain.LoadingSheet;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetItem;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetReturnItem;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetState;
import com.spiceflow.backend.sales.loading.entity.LoadingSheetWorkflowEntity;
import com.spiceflow.backend.sales.loading.repository.LoadingSheetWorkflowRepository;
import com.spiceflow.backend.sales.loading.workflow.command.ConfirmLoadingSheetCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
class LoadingSheetWorkflowServiceTest {

    @Mock private LoadingSheetWorkflowRepository repository;
    @Mock private LoadingSheetPersistenceAdapter adapter;
    private WorkflowEngine engine = new WorkflowEngine();
    @Mock private InventoryLedgerService ledgerService;
    @Mock private DomainEventPublisher eventPublisher;
    @Mock private AuditService auditService;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private TenantRepository tenantRepository;

    private LoadingSheetWorkflowService service;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        service = new LoadingSheetWorkflowService(
                repository, adapter, engine, ledgerService, eventPublisher, auditService, warehouseRepository, tenantRepository
        );
        context = new WorkflowContext(1L, 10L, "LS-2026-0001", Instant.now());
    }

    @Test
    void should_create_loading_sheet_successfully() {
        LoadingSheet ls = LoadingSheet.create("LS-2026-0001", 10L, 100L, "RO-2026-0001", 5L, "John Driver", LocalDate.now(), "admin", List.of(), List.of());
        LoadingSheetWorkflowEntity entity = new LoadingSheetWorkflowEntity();
        when(adapter.toEntity(ls)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(adapter.toDomain(entity)).thenReturn(ls);

        LoadingSheet created = service.createLoadingSheet(ls);

        assertNotNull(created);
        assertEquals("LS-2026-0001", created.getSheetNumber());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void should_confirm_loading_sheet_and_record_double_entry_movements() {
        LoadingSheetItem item = new LoadingSheetItem(1L, 101L, 50, "PACK");
        LoadingSheetReturnItem ret = new LoadingSheetReturnItem(2L, 102L, 5, "BOX", "DAMAGED");
        LoadingSheet ls = LoadingSheet.create("LS-2026-0001", 10L, 100L, "RO-2026-0001", 5L, "John Driver", LocalDate.now(), "admin", List.of(item), List.of(ret));

        LoadingSheetWorkflowEntity entity = new LoadingSheetWorkflowEntity();
        when(repository.findBySheetNumberAndTenantId("LS-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toDomain(entity)).thenReturn(ls);
        when(adapter.toEntity(any())).thenReturn(entity);

        Warehouse mainStore = new Warehouse();
        mainStore.setId(1L);
        mainStore.setName("Main Store");
        mainStore.setStoreType("MAIN");

        Warehouse vehicleStore = new Warehouse();
        vehicleStore.setId(2L);
        vehicleStore.setName("Vehicle - John Driver");
        vehicleStore.setStoreType("CUSTOM");

        when(warehouseRepository.findAllByTenantId(10L)).thenReturn(List.of(mainStore, vehicleStore));

        WorkflowResult<LoadingSheet> result = service.executeCommand(
                "LS-2026-0001",
                10L,
                new ConfirmLoadingSheetCommand("Confirmed"),
                context
        );

        assertEquals(LoadingSheetState.CONFIRMED, result.updatedAggregate().getState());
        verify(eventPublisher, times(1)).publish(any());
        verify(auditService, times(1)).record(any());

        // Verify TRANSFER_OUT from MAIN (-50)
        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(1L), eq(101L), eq(InventoryMovementType.TRANSFER_OUT), eq(new BigDecimal("-50")), eq(BigDecimal.ZERO), eq("LS-2026-0001"), eq(""), isNull(), any(), eq("1")
        );

        // Verify TRANSFER_IN to VEHICLE (+50)
        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(2L), eq(101L), eq(InventoryMovementType.TRANSFER_IN), eq(new BigDecimal("50")), eq(BigDecimal.ZERO), eq("LS-2026-0001"), eq(""), isNull(), any(), eq("1")
        );

        // Verify DAMAGED return to MAIN (+5)
        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(1L), eq(102L), eq(InventoryMovementType.DAMAGED), eq(new BigDecimal("5")), eq(BigDecimal.ZERO), eq("LS-2026-0001"), eq(""), isNull(), any(), eq("1")
        );
    }
}
