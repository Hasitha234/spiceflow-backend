package com.spiceflow.backend.sales.delivery.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.events.DomainEvent;
import com.spiceflow.backend.events.DomainEventPublisher;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.sales.delivery.adapter.DeliveryPersistenceAdapter;
import com.spiceflow.backend.sales.delivery.domain.Delivery;
import com.spiceflow.backend.sales.delivery.domain.DeliveryReturnItemRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryShopItemRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryShopRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryState;
import com.spiceflow.backend.sales.delivery.entity.DeliveryWorkflowEntity;
import com.spiceflow.backend.sales.delivery.repository.DeliveryWorkflowRepository;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.repository.ShopRepository;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operational workflow service for Customer Deliveries.
 *
 * Orchestrates FSM transitions via {@link WorkflowEngine}, event publishing,
 * audit logging, and automated side-effects:
 *
 * <ul>
 *   <li><b>On DISPATCHED</b>: Records {@code DELIVERY} stock movements (deductions) from
 *       the driver's vehicle warehouse for every delivered line item.</li>
 *   <li><b>On COMPLETED</b>: Records {@code RECEIPT}/{@code DAMAGED} movements for
 *       returned items back into the vehicle warehouse, then posts credit amounts
 *       (unpaid net payable) to the respective shop's outstanding loan.</li>
 * </ul>
 */
@Slf4j
@Service
public class DeliveryWorkflowService {

    private final DeliveryWorkflowRepository repository;
    private final DeliveryPersistenceAdapter adapter;
    private final WorkflowEngine engine;
    private final InventoryLedgerService ledgerService;
    private final DomainEventPublisher eventPublisher;
    private final AuditService auditService;
    private final WarehouseRepository warehouseRepository;
    private final ShopRepository shopRepository;

    public DeliveryWorkflowService(DeliveryWorkflowRepository repository,
                                   DeliveryPersistenceAdapter adapter,
                                   WorkflowEngine engine,
                                   InventoryLedgerService ledgerService,
                                   DomainEventPublisher eventPublisher,
                                   AuditService auditService,
                                   WarehouseRepository warehouseRepository,
                                   ShopRepository shopRepository) {
        this.repository = repository;
        this.adapter = adapter;
        this.engine = engine;
        this.ledgerService = ledgerService;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.warehouseRepository = warehouseRepository;
        this.shopRepository = shopRepository;
    }

    @Transactional
    public Delivery createDelivery(Delivery delivery) {
        DeliveryWorkflowEntity entity = adapter.toEntity(delivery);
        DeliveryWorkflowEntity saved = repository.save(entity);
        log.info("Created delivery {} for tenant {}", delivery.getDeliveryNumber(), delivery.getTenantId());
        return adapter.toDomain(saved);
    }

    @Transactional(readOnly = true)
    public Delivery getDelivery(String deliveryNumber, Long tenantId) {
        DeliveryWorkflowEntity entity = repository.findByDeliveryNumberAndTenantId(deliveryNumber, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryNumber));
        return adapter.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Delivery getDeliveryById(Long id, Long tenantId) {
        DeliveryWorkflowEntity entity = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
        return adapter.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public List<Delivery> listDeliveries(Long tenantId, DeliveryState status) {
        return repository.findAllByTenantId(tenantId).stream()
                .filter(e -> e.getStatus() == status)
                .map(adapter::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Delivery> listAllDeliveries(Long tenantId) {
        return repository.findAllByTenantId(tenantId).stream()
                .map(adapter::toDomain)
                .toList();
    }

    @Transactional
    public WorkflowResult<Delivery> executeCommand(String deliveryNumber,
                                                    Long tenantId,
                                                    WorkflowCommand<Delivery, DeliveryState> command,
                                                    WorkflowContext context) {
        Delivery current = getDelivery(deliveryNumber, tenantId);
        WorkflowResult<Delivery> result = engine.execute(command, current, context);

        DeliveryWorkflowEntity updatedEntity = adapter.toEntity(result.updatedAggregate());
        repository.save(updatedEntity);

        if (result.auditEntry() != null) {
            auditService.record(result.auditEntry());
        }

        for (DomainEvent event : result.events()) {
            eventPublisher.publish(event);
        }

        Delivery updated = result.updatedAggregate();
        DeliveryState newState = updated.getState();

        if (newState == DeliveryState.DISPATCHED) {
            recordDispatchLedgerMovements(updated, context);
        }

        if (newState == DeliveryState.COMPLETED) {
            recordReturnLedgerMovements(updated, context);
            postCreditToShopOutstandingBalance(updated, tenantId);
        }

        log.info("Delivery {} transitioned to {} for tenant {}", deliveryNumber, newState, tenantId);
        return result;
    }

    // ─── PRIVATE: Ledger & Receivables accounting helpers ───────────────────────

    /**
     * On DISPATCH: deduct delivered quantities from the driver's vehicle warehouse.
     * Uses {@link InventoryMovementType#DELIVERY} to record outflows to customer shops.
     */
    private void recordDispatchLedgerMovements(Delivery delivery, WorkflowContext context) {
        Long vehicleStoreId = resolveVehicleWarehouseId(delivery.getTenantId());

        for (DeliveryShopRecord shop : delivery.getShops()) {
            for (DeliveryShopItemRecord item : shop.items()) {
                if (!item.isFreeItem() && item.quantityDelivered() > 0) {
                    ledgerService.recordMovement(
                            delivery.getTenantId(),
                            vehicleStoreId,
                            item.productId(),
                            InventoryMovementType.DELIVERY,
                            new BigDecimal(item.quantityDelivered()).negate(),
                            item.netAmount(),
                            delivery.getDeliveryNumber(),
                            "Shop ID: " + shop.shopId(),
                            null,
                            context.timestamp(),
                            String.valueOf(context.userId())
                    );
                }
            }
        }
    }

    /**
     * On COMPLETION: credit returned items back into the vehicle warehouse.
     * DAMAGED items use {@link InventoryMovementType#DAMAGED};
     * all others use {@link InventoryMovementType#RECEIPT}.
     */
    private void recordReturnLedgerMovements(Delivery delivery, WorkflowContext context) {
        Long vehicleStoreId = resolveVehicleWarehouseId(delivery.getTenantId());

        for (DeliveryShopRecord shop : delivery.getShops()) {
            for (DeliveryReturnItemRecord ret : shop.returns()) {
                if (ret.quantityReturned() > 0) {
                    InventoryMovementType movementType = "DAMAGED".equalsIgnoreCase(ret.returnType())
                            ? InventoryMovementType.DAMAGED
                            : InventoryMovementType.RECEIPT;

                    ledgerService.recordMovement(
                            delivery.getTenantId(),
                            vehicleStoreId,
                            ret.productId(),
                            movementType,
                            new BigDecimal(ret.quantityReturned()),
                            ret.creditValue(),
                            delivery.getDeliveryNumber(),
                            "Return from shop ID: " + shop.shopId(),
                            null,
                            context.timestamp(),
                            String.valueOf(context.userId())
                    );
                }
            }
        }
    }

    /**
     * On COMPLETION: for each shop, if credit was extended (netPayable > paidAmount),
     * add the credit amount to the shop's outstanding loan balance.
     */
    private void postCreditToShopOutstandingBalance(Delivery delivery, Long tenantId) {
        for (DeliveryShopRecord shopRecord : delivery.getShops()) {
            BigDecimal credit = shopRecord.creditAmount();
            if (credit.compareTo(BigDecimal.ZERO) > 0) {
                Shop shop = shopRepository.findByIdAndTenantId(shopRecord.shopId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopRecord.shopId()));
                shop.setOutstandingLoan(shop.getOutstandingLoan().add(credit));
                shopRepository.save(shop);
                log.debug("Posted credit {} to shop {} outstanding balance", credit, shopRecord.shopId());
            }
        }
    }

    /**
     * Resolves the vehicle (van) warehouse for this delivery's tenant.
     * Vehicle warehouses are tagged CUSTOM and created by LoadingSheetWorkflowService on confirm.
     * Falls back to the MAIN warehouse to avoid NPE if no vehicle warehouse is found.
     */
    private Long resolveVehicleWarehouseId(Long tenantId) {
        return warehouseRepository.findAllByTenantId(tenantId).stream()
                .filter(w -> "CUSTOM".equals(w.getStoreType()))
                .map(Warehouse::getId)
                .findFirst()
                .orElseGet(() ->
                    warehouseRepository.findAllByTenantId(tenantId).stream()
                            .filter(w -> "MAIN".equals(w.getStoreType()) || Boolean.TRUE.equals(w.getIsSystemStore()))
                            .map(Warehouse::getId)
                            .findFirst()
                            .orElse(1L)
                );
    }
}
