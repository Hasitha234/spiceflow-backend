package com.spiceflow.backend.sales.delivery.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.events.DomainEventPublisher;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.sales.delivery.adapter.DeliveryPersistenceAdapter;
import com.spiceflow.backend.sales.delivery.domain.Delivery;
import com.spiceflow.backend.sales.delivery.domain.DeliveryPaymentRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryReturnItemRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryShopItemRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryShopRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryState;
import com.spiceflow.backend.sales.delivery.entity.DeliveryWorkflowEntity;
import com.spiceflow.backend.sales.delivery.repository.DeliveryWorkflowRepository;
import com.spiceflow.backend.sales.delivery.workflow.command.CancelDeliveryCommand;
import com.spiceflow.backend.sales.delivery.workflow.command.CompleteDeliveryCommand;
import com.spiceflow.backend.sales.delivery.workflow.command.DispatchDeliveryCommand;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.repository.ShopRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryWorkflowServiceTest {

    @Mock private DeliveryWorkflowRepository repository;
    @Mock private DeliveryPersistenceAdapter adapter;
    private WorkflowEngine engine = new WorkflowEngine();
    @Mock private InventoryLedgerService ledgerService;
    @Mock private DomainEventPublisher eventPublisher;
    @Mock private AuditService auditService;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private ShopRepository shopRepository;

    private DeliveryWorkflowService service;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        service = new DeliveryWorkflowService(
                repository, adapter, engine, ledgerService,
                eventPublisher, auditService, warehouseRepository, shopRepository
        );
        context = new WorkflowContext(1L, 10L, "DEL-2026-0001", Instant.now());
    }

    @Test
    void should_create_delivery_successfully() {
        Delivery delivery = Delivery.create("DEL-2026-0001", 10L, 200L, "LS-2026-0001",
                LocalDate.now(), "admin", List.of());
        DeliveryWorkflowEntity entity = new DeliveryWorkflowEntity();
        when(adapter.toEntity(delivery)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(adapter.toDomain(entity)).thenReturn(delivery);

        Delivery created = service.createDelivery(delivery);

        assertNotNull(created);
        assertEquals("DEL-2026-0001", created.getDeliveryNumber());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void should_dispatch_delivery_and_record_delivery_ledger_movements() {
        DeliveryShopItemRecord item = new DeliveryShopItemRecord(
                1L, 101L, 20, "PACK",
                BigDecimal.valueOf(500), BigDecimal.valueOf(10000),
                BigDecimal.ZERO, BigDecimal.valueOf(10000), false
        );
        DeliveryShopRecord shop = new DeliveryShopRecord(
                5L, 201L,
                BigDecimal.valueOf(10000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), BigDecimal.ZERO,
                List.of(item), List.of(), List.of()
        );
        Delivery delivery = Delivery.create("DEL-2026-0001", 10L, 200L, "LS-2026-0001",
                LocalDate.now(), "admin", List.of(shop));

        DeliveryWorkflowEntity entity = new DeliveryWorkflowEntity();
        when(repository.findByDeliveryNumberAndTenantId("DEL-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toDomain(entity)).thenReturn(delivery);
        when(adapter.toEntity(any())).thenReturn(entity);

        Warehouse vehicleStore = new Warehouse();
        vehicleStore.setId(99L);
        vehicleStore.setStoreType("CUSTOM");
        when(warehouseRepository.findAllByTenantId(10L)).thenReturn(List.of(vehicleStore));

        WorkflowResult<Delivery> result = service.executeCommand(
                "DEL-2026-0001", 10L,
                new DispatchDeliveryCommand("Route started"),
                context
        );

        assertEquals(DeliveryState.DISPATCHED, result.updatedAggregate().getState());
        verify(eventPublisher, times(1)).publish(any());
        verify(auditService, times(1)).record(any());

        // Verify DELIVERY movement (-20 units) from vehicle warehouse
        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(99L), eq(101L),
                eq(InventoryMovementType.DELIVERY),
                eq(new BigDecimal("20").negate()),
                eq(BigDecimal.valueOf(10000)),
                eq("DEL-2026-0001"),
                eq("Shop ID: 201"),
                any(), any(), eq("1")
        );
    }

    @Test
    void should_complete_delivery_record_returns_and_post_credit_to_shop_balance() {
        DeliveryReturnItemRecord ret = new DeliveryReturnItemRecord(
                2L, 102L, 5, "PACK", BigDecimal.valueOf(500), "EXPIRED"
        );
        DeliveryPaymentRecord payment = new DeliveryPaymentRecord(
                3L, "CASH", BigDecimal.valueOf(800), null, null, null
        );
        // Credit = 200 (netPayable=1000 - paidAmount=800)
        DeliveryShopRecord shop = new DeliveryShopRecord(
                5L, 201L,
                BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.valueOf(200),
                BigDecimal.valueOf(800), BigDecimal.valueOf(800), BigDecimal.valueOf(200),
                List.of(), List.of(ret), List.of(payment)
        );
        Delivery delivery = new Delivery(
                Delivery.create("DEL-2026-0001", 10L, 200L, "LS-2026-0001",
                        LocalDate.now(), "admin", List.of(shop)),
                DeliveryState.DISPATCHED, List.of(shop)
        );

        DeliveryWorkflowEntity entity = new DeliveryWorkflowEntity();
        when(repository.findByDeliveryNumberAndTenantId("DEL-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toDomain(entity)).thenReturn(delivery);
        when(adapter.toEntity(any())).thenReturn(entity);

        Warehouse vehicleStore = new Warehouse();
        vehicleStore.setId(99L);
        vehicleStore.setStoreType("CUSTOM");
        when(warehouseRepository.findAllByTenantId(10L)).thenReturn(List.of(vehicleStore));

        Shop shopEntity = new Shop();
        shopEntity.setOutstandingLoan(BigDecimal.ZERO);
        when(shopRepository.findByIdAndTenantId(201L, 10L)).thenReturn(Optional.of(shopEntity));

        WorkflowResult<Delivery> result = service.executeCommand(
                "DEL-2026-0001", 10L,
                new CompleteDeliveryCommand("All shops done"),
                context
        );

        assertEquals(DeliveryState.COMPLETED, result.updatedAggregate().getState());

        // Verify RECEIPT movement (+5 units) for returned items
        verify(ledgerService, times(1)).recordMovement(
                eq(10L), eq(99L), eq(102L),
                eq(InventoryMovementType.RECEIPT),
                eq(new BigDecimal("5")),
                eq(BigDecimal.valueOf(500)),
                eq("DEL-2026-0001"),
                eq("Return from shop ID: 201"),
                any(), any(), eq("1")
        );

        // Verify credit posted to shop outstanding balance
        verify(shopRepository, times(1)).save(any(Shop.class));
        assertEquals(BigDecimal.valueOf(200), shopEntity.getOutstandingLoan());
    }

    @Test
    void should_cancel_delivery_without_ledger_movements() {
        Delivery delivery = Delivery.create("DEL-2026-0001", 10L, 200L, "LS-2026-0001",
                LocalDate.now(), "admin", List.of());
        DeliveryWorkflowEntity entity = new DeliveryWorkflowEntity();
        when(repository.findByDeliveryNumberAndTenantId("DEL-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toDomain(entity)).thenReturn(delivery);
        when(adapter.toEntity(any())).thenReturn(entity);

        WorkflowResult<Delivery> result = service.executeCommand(
                "DEL-2026-0001", 10L,
                new CancelDeliveryCommand("Customer not available"),
                context
        );

        assertEquals(DeliveryState.CANCELLED, result.updatedAggregate().getState());
        // No ledger movements on cancel
        verify(ledgerService, times(0)).recordMovement(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
