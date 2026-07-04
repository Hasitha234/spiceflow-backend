package com.spiceflow.backend.sales.order.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.events.DomainEventPublisher;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.sales.order.adapter.RepOrderPersistenceAdapter;
import com.spiceflow.backend.sales.order.domain.RepOrder;
import com.spiceflow.backend.sales.order.domain.RepOrderItem;
import com.spiceflow.backend.sales.order.domain.RepOrderShop;
import com.spiceflow.backend.sales.order.domain.RepOrderState;
import com.spiceflow.backend.sales.order.domain.ShopReturnItem;
import com.spiceflow.backend.sales.order.entity.RepOrderWorkflowEntity;
import com.spiceflow.backend.sales.order.repository.RepOrderWorkflowRepository;
import com.spiceflow.backend.sales.order.workflow.command.ApproveRepOrderCommand;
import com.spiceflow.backend.sales.order.workflow.command.DeliverRepOrderCommand;
import com.spiceflow.backend.sales.order.workflow.command.SubmitRepOrderCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
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
class RepOrderWorkflowServiceTest {

    @Mock private RepOrderWorkflowRepository repository;
    @Mock private RepOrderPersistenceAdapter adapter;
    private WorkflowEngine engine = new WorkflowEngine();
    @Mock private InventoryLedgerService ledgerService;
    @Mock private DomainEventPublisher eventPublisher;
    @Mock private AuditService auditService;
    @Mock private WarehouseRepository warehouseRepository;

    private RepOrderWorkflowService service;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        service = new RepOrderWorkflowService(
                repository, adapter, engine, ledgerService, eventPublisher, auditService, warehouseRepository
        );
        context = new WorkflowContext(1L, 10L, "RO-2026-0001", Instant.now());
    }

    @Test
    void should_create_rep_order_successfully() {
        RepOrder ro = RepOrder.create("RO-2026-0001", 10L, 5L, LocalDate.now(), "North Route", "admin");
        RepOrderWorkflowEntity entity = new RepOrderWorkflowEntity();
        when(adapter.toEntity(ro)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(adapter.toDomain(entity)).thenReturn(ro);

        RepOrder created = service.createRepOrder(ro);

        assertNotNull(created);
        assertEquals("RO-2026-0001", created.getOrderNumber());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void should_execute_submit_and_approve_commands() {
        RepOrderItem item = new RepOrderItem(
                100L, 10, "PACK", new BigDecimal("150.00"), BigDecimal.ZERO, false, 1
        );
        RepOrderShop shop = new RepOrderShop(50L, List.of(item), Collections.emptyList());
        RepOrder draftRo = new RepOrder(
                RepOrder.create("RO-2026-0001", 10L, 5L, LocalDate.now(), "North Route", "admin"),
                RepOrderState.DRAFT,
                List.of(shop)
        );
        RepOrderWorkflowEntity entity = new RepOrderWorkflowEntity();

        when(repository.findByOrderNumberAndTenantId("RO-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toDomain(entity)).thenReturn(draftRo);
        when(adapter.toEntity(any(RepOrder.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        service.executeCommand("RO-2026-0001", 10L, new SubmitRepOrderCommand(), context);

        verify(repository, times(1)).save(entity);
        verify(auditService, times(1)).record(any());
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void should_record_delivery_and_return_movements_when_delivered() {
        RepOrderItem item = new RepOrderItem(
                100L, 10, "PACK", new BigDecimal("150.00"), BigDecimal.ZERO, false, 1
        );
        ShopReturnItem returnItem = new ShopReturnItem(
                200L, 2, "PACK", new BigDecimal("100.00"), "DAMAGED"
        );
        RepOrderShop shop = new RepOrderShop(50L, List.of(item), List.of(returnItem));
        RepOrder loadedRo = new RepOrder(
                RepOrder.create("RO-2026-0001", 10L, 5L, LocalDate.now(), "North Route", "admin"),
                RepOrderState.LOADED,
                List.of(shop)
        );
        RepOrderWorkflowEntity entity = new RepOrderWorkflowEntity();
        Warehouse warehouse = new Warehouse();
        warehouse.setId(5L);
        warehouse.setStoreType("MAIN");

        when(repository.findByOrderNumberAndTenantId("RO-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toDomain(entity)).thenReturn(loadedRo);
        when(adapter.toEntity(any(RepOrder.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(warehouseRepository.findAllByTenantId(10L)).thenReturn(List.of(warehouse));

        service.executeCommand("RO-2026-0001", 10L, new DeliverRepOrderCommand(), context);

        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(5L), eq(100L), eq(InventoryMovementType.DELIVERY),
                eq(new BigDecimal("-10")), eq(new BigDecimal("150.00")), eq("RO-2026-0001"),
                eq(""), isNull(), any(Instant.class), eq("1")
        );

        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(5L), eq(200L), eq(InventoryMovementType.DAMAGED),
                eq(new BigDecimal("2")), eq(new BigDecimal("100.00")), eq("RO-2026-0001"),
                eq(""), isNull(), any(Instant.class), eq("1")
        );
    }
}
