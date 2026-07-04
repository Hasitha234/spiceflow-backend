package com.spiceflow.backend.inventory.transfer.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.events.DomainEvent;
import com.spiceflow.backend.events.DomainEventPublisher;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.transfer.adapter.WarehouseTransferPersistenceAdapter;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransfer;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferLine;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
import com.spiceflow.backend.inventory.transfer.entity.WarehouseTransferEntity;
import com.spiceflow.backend.inventory.transfer.repository.WarehouseTransferRepository;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseTransferService {

    private final WarehouseTransferRepository repository;
    private final WarehouseTransferPersistenceAdapter adapter;
    private final WorkflowEngine engine;
    private final InventoryLedgerService ledgerService;
    private final DomainEventPublisher eventPublisher;
    private final AuditService auditService;

    public WarehouseTransferService(WarehouseTransferRepository repository,
                                    WarehouseTransferPersistenceAdapter adapter,
                                    WorkflowEngine engine,
                                    InventoryLedgerService ledgerService,
                                    DomainEventPublisher eventPublisher,
                                    AuditService auditService) {
        this.repository = repository;
        this.adapter = adapter;
        this.engine = engine;
        this.ledgerService = ledgerService;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    @Transactional
    public WarehouseTransfer createTransfer(WarehouseTransfer transfer) {
        WarehouseTransferEntity entity = adapter.toEntity(transfer);
        WarehouseTransferEntity saved = repository.save(entity);
        return adapter.toDomain(saved);
    }

    @Transactional(readOnly = true)
    public WarehouseTransfer getTransfer(String transferNumber, Long tenantId) {
        WarehouseTransferEntity entity = repository.findByTransferNumberAndTenantId(transferNumber, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse Transfer not found: " + transferNumber));
        return adapter.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public List<WarehouseTransfer> listTransfers(Long tenantId, @Nullable WarehouseTransferState status) {
        List<WarehouseTransferEntity> entities = status != null
                ? repository.findByTenantIdAndStatus(tenantId, status)
                : repository.findAll().stream().filter(e -> e.getTenantId().equals(tenantId)).toList();
        return entities.stream().map(adapter::toDomain).toList();
    }

    @Transactional
    public WorkflowResult<WarehouseTransfer> executeCommand(String transferNumber,
                                                            Long tenantId,
                                                            WorkflowCommand<WarehouseTransfer, WarehouseTransferState> command,
                                                            WorkflowContext context) {
        WarehouseTransfer current = getTransfer(transferNumber, tenantId);
        WorkflowResult<WarehouseTransfer> result = engine.execute(command, current, context);

        WarehouseTransferEntity updatedEntity = adapter.toEntity(result.updatedAggregate());
        repository.save(updatedEntity);

        if (result.auditEntry() != null) {
            auditService.record(result.auditEntry());
        }

        for (DomainEvent event : result.events()) {
            eventPublisher.publish(event);
        }

        WarehouseTransfer updated = result.updatedAggregate();
        if (updated.getState() == WarehouseTransferState.SHIPPED) {
            for (WarehouseTransferLine line : updated.getLines()) {
                if (line.getShippedQty().compareTo(BigDecimal.ZERO) > 0) {
                    ledgerService.recordMovement(
                            tenantId,
                            updated.getFromWarehouseId(),
                            line.getProductId(),
                            InventoryMovementType.TRANSFER_OUT,
                            line.getShippedQty().negate(),
                            line.getUnitCost(),
                            transferNumber,
                            line.getLotNumber(),
                            null,
                            context.timestamp(),
                            String.valueOf(context.userId())
                    );
                }
            }
        } else if (updated.getState() == WarehouseTransferState.RECEIVED) {
            for (WarehouseTransferLine line : updated.getLines()) {
                if (line.getReceivedQty().compareTo(BigDecimal.ZERO) > 0) {
                    ledgerService.recordMovement(
                            tenantId,
                            updated.getToWarehouseId(),
                            line.getProductId(),
                            InventoryMovementType.TRANSFER_IN,
                            line.getReceivedQty(),
                            line.getUnitCost(),
                            transferNumber,
                            line.getLotNumber(),
                            null,
                            context.timestamp(),
                            String.valueOf(context.userId())
                    );
                }
                if (line.getDamagedQty().compareTo(BigDecimal.ZERO) > 0) {
                    ledgerService.recordMovement(
                            tenantId,
                            updated.getToWarehouseId(),
                            line.getProductId(),
                            InventoryMovementType.DAMAGED,
                            line.getDamagedQty().negate(),
                            line.getUnitCost(),
                            transferNumber,
                            line.getLotNumber(),
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
