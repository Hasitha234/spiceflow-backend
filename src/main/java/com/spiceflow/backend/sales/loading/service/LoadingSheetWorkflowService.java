package com.spiceflow.backend.sales.loading.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.events.DomainEvent;
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
 * Operational workflow service for Van Loading Sheets.
 * Coordinates FSM transitions, event publishing, audit logging, and double-entry stock ledger movements.
 */
@Service
public class LoadingSheetWorkflowService {

    private final LoadingSheetWorkflowRepository repository;
    private final LoadingSheetPersistenceAdapter adapter;
    private final WorkflowEngine engine;
    private final InventoryLedgerService ledgerService;
    private final DomainEventPublisher eventPublisher;
    private final AuditService auditService;
    private final WarehouseRepository warehouseRepository;
    private final TenantRepository tenantRepository;

    public LoadingSheetWorkflowService(LoadingSheetWorkflowRepository repository,
                                       LoadingSheetPersistenceAdapter adapter,
                                       WorkflowEngine engine,
                                       InventoryLedgerService ledgerService,
                                       DomainEventPublisher eventPublisher,
                                       AuditService auditService,
                                       WarehouseRepository warehouseRepository,
                                       TenantRepository tenantRepository) {
        this.repository = repository;
        this.adapter = adapter;
        this.engine = engine;
        this.ledgerService = ledgerService;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.warehouseRepository = warehouseRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public LoadingSheet createLoadingSheet(LoadingSheet loadingSheet) {
        LoadingSheetWorkflowEntity entity = adapter.toEntity(loadingSheet);
        LoadingSheetWorkflowEntity saved = repository.save(entity);
        return adapter.toDomain(saved);
    }

    @Transactional(readOnly = true)
    public LoadingSheet getLoadingSheet(String sheetNumber, Long tenantId) {
        LoadingSheetWorkflowEntity entity = repository.findBySheetNumberAndTenantId(sheetNumber, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Loading Sheet not found: " + sheetNumber));
        return adapter.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public List<LoadingSheet> listLoadingSheets(Long tenantId, @Nullable LoadingSheetState status) {
        List<LoadingSheetWorkflowEntity> entities = repository.findAll().stream()
                .filter(e -> e.getTenantId().equals(tenantId))
                .filter(e -> status == null || e.getStatus() == status)
                .toList();
        return entities.stream().map(adapter::toDomain).toList();
    }

    @Transactional
    public WorkflowResult<LoadingSheet> executeCommand(String sheetNumber,
                                                       Long tenantId,
                                                       WorkflowCommand<LoadingSheet, LoadingSheetState> command,
                                                       WorkflowContext context) {
        LoadingSheet current = getLoadingSheet(sheetNumber, tenantId);
        WorkflowResult<LoadingSheet> result = engine.execute(command, current, context);

        LoadingSheetWorkflowEntity updatedEntity = adapter.toEntity(result.updatedAggregate());
        repository.save(updatedEntity);

        if (result.auditEntry() != null) {
            auditService.record(result.auditEntry());
        }

        for (DomainEvent event : result.events()) {
            eventPublisher.publish(event);
        }

        LoadingSheet updated = result.updatedAggregate();
        if (updated.getState() == LoadingSheetState.CONFIRMED) {
            Long mainWarehouseId = warehouseRepository.findAllByTenantId(tenantId).stream()
                    .filter(w -> "MAIN".equals(w.getStoreType()) || "DEFAULT".equals(w.getStoreType()) || Boolean.TRUE.equals(w.getIsSystemStore()))
                    .map(Warehouse::getId)
                    .findFirst()
                    .orElse(1L);

            String vehicleStoreName = "Vehicle - " + (updated.getDriverName().isEmpty() ? "Driver " + updated.getDriverId() : updated.getDriverName());
            Warehouse vehicleStore = warehouseRepository.findAllByTenantId(tenantId).stream()
                    .filter(w -> "CUSTOM".equals(w.getStoreType()) && vehicleStoreName.equals(w.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        Tenant tenant = tenantRepository.findById(tenantId)
                                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + tenantId));
                        Warehouse newStore = Warehouse.builder()
                                .tenant(tenant)
                                .name(vehicleStoreName)
                                .storeType("CUSTOM")
                                .isSystemStore(false)
                                .description("Store for vehicle/driver " + updated.getDriverId())
                                .build();
                        return warehouseRepository.save(newStore);
                    });
            Long vehicleStoreId = vehicleStore.getId();

            for (LoadingSheetItem item : updated.getItems()) {
                if (item.quantityLoaded() > 0) {
                    // TRANSFER_OUT from MAIN warehouse
                    ledgerService.recordMovement(
                            tenantId,
                            mainWarehouseId,
                            item.productId(),
                            InventoryMovementType.TRANSFER_OUT,
                            new BigDecimal(item.quantityLoaded()).negate(),
                            BigDecimal.ZERO,
                            sheetNumber,
                            "",
                            null,
                            context.timestamp(),
                            String.valueOf(context.userId())
                    );

                    // TRANSFER_IN to VEHICLE warehouse
                    ledgerService.recordMovement(
                            tenantId,
                            vehicleStoreId,
                            item.productId(),
                            InventoryMovementType.TRANSFER_IN,
                            new BigDecimal(item.quantityLoaded()),
                            BigDecimal.ZERO,
                            sheetNumber,
                            "",
                            null,
                            context.timestamp(),
                            String.valueOf(context.userId())
                    );
                }
            }

            for (LoadingSheetReturnItem r : updated.getReturns()) {
                if (r.quantityReturned() > 0) {
                    InventoryMovementType movementType = "DAMAGED".equalsIgnoreCase(r.returnType())
                            ? InventoryMovementType.DAMAGED
                            : InventoryMovementType.RECEIPT;

                    // Return from vehicle back to MAIN warehouse
                    ledgerService.recordMovement(
                            tenantId,
                            mainWarehouseId,
                            r.productId(),
                            movementType,
                            new BigDecimal(r.quantityReturned()),
                            BigDecimal.ZERO,
                            sheetNumber,
                            "",
                            null,
                            context.timestamp(),
                            String.valueOf(context.userId())
                    );
                }
            }
        }

        return result;
    }
}
