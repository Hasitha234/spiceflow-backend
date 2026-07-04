package com.spiceflow.backend.sales.order.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.events.DomainEvent;
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
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operational workflow service for Rep Orders.
 * Coordinates FSM transitions, event publishing, audit logging, and inventory movements.
 */
@Service
public class RepOrderWorkflowService {

    private final RepOrderWorkflowRepository repository;
    private final RepOrderPersistenceAdapter adapter;
    private final WorkflowEngine engine;
    private final InventoryLedgerService ledgerService;
    private final DomainEventPublisher eventPublisher;
    private final AuditService auditService;
    private final WarehouseRepository warehouseRepository;

    public RepOrderWorkflowService(RepOrderWorkflowRepository repository,
                                   RepOrderPersistenceAdapter adapter,
                                   WorkflowEngine engine,
                                   InventoryLedgerService ledgerService,
                                   DomainEventPublisher eventPublisher,
                                   AuditService auditService,
                                   WarehouseRepository warehouseRepository) {
        this.repository = repository;
        this.adapter = adapter;
        this.engine = engine;
        this.ledgerService = ledgerService;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public RepOrder createRepOrder(RepOrder repOrder) {
        RepOrderWorkflowEntity entity = adapter.toEntity(repOrder);
        RepOrderWorkflowEntity saved = repository.save(entity);
        return adapter.toDomain(saved);
    }

    @Transactional(readOnly = true)
    public RepOrder getRepOrder(String orderNumber, Long tenantId) {
        RepOrderWorkflowEntity entity = repository.findByOrderNumberAndTenantId(orderNumber, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep Order not found: " + orderNumber));
        return adapter.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public List<RepOrder> listRepOrders(Long tenantId, @Nullable RepOrderState status) {
        List<RepOrderWorkflowEntity> entities = repository.findAll().stream()
                .filter(e -> e.getTenantId().equals(tenantId))
                .filter(e -> status == null || e.getStatus() == status)
                .toList();
        return entities.stream().map(adapter::toDomain).toList();
    }

    @Transactional
    public WorkflowResult<RepOrder> executeCommand(String orderNumber,
                                                   Long tenantId,
                                                   WorkflowCommand<RepOrder, RepOrderState> command,
                                                   WorkflowContext context) {
        RepOrder current = getRepOrder(orderNumber, tenantId);
        WorkflowResult<RepOrder> result = engine.execute(command, current, context);

        RepOrderWorkflowEntity updatedEntity = adapter.toEntity(result.updatedAggregate());
        repository.save(updatedEntity);

        if (result.auditEntry() != null) {
            auditService.record(result.auditEntry());
        }

        for (DomainEvent event : result.events()) {
            eventPublisher.publish(event);
        }

        RepOrder updated = result.updatedAggregate();
        if (updated.getState() == RepOrderState.DELIVERED) {
            Long warehouseId = warehouseRepository.findAllByTenantId(tenantId).stream()
                    .filter(w -> "MAIN".equals(w.getStoreType()) || "DEFAULT".equals(w.getStoreType()) || Boolean.TRUE.equals(w.getIsSystemStore()))
                    .map(Warehouse::getId)
                    .findFirst()
                    .orElse(1L);

            for (RepOrderShop shop : updated.getShops()) {
                for (RepOrderItem item : shop.items()) {
                    if (item.quantity() > 0) {
                        ledgerService.recordMovement(
                                tenantId,
                                warehouseId,
                                item.productId(),
                                InventoryMovementType.DELIVERY,
                                new BigDecimal(item.quantity()).negate(),
                                item.rate(),
                                orderNumber,
                                "",
                                null,
                                context.timestamp(),
                                String.valueOf(context.userId())
                        );
                    }
                }

                for (ShopReturnItem r : shop.returns()) {
                    if (r.quantity() > 0) {
                        InventoryMovementType movementType = "DAMAGED".equalsIgnoreCase(r.returnType())
                                ? InventoryMovementType.DAMAGED
                                : InventoryMovementType.RECEIPT;
                        ledgerService.recordMovement(
                                tenantId,
                                warehouseId,
                                r.productId(),
                                movementType,
                                new BigDecimal(r.quantity()),
                                r.creditValue(),
                                orderNumber,
                                "",
                                null,
                                context.timestamp(),
                                String.valueOf(context.userId())
                        );
                    }
                }
            }
        }

        return result;
    }
}
