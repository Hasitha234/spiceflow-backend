package com.spiceflow.backend.inventory.transfer.adapter;

import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransfer;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferLine;
import com.spiceflow.backend.inventory.transfer.entity.WarehouseTransferEntity;
import com.spiceflow.backend.inventory.transfer.entity.WarehouseTransferLineEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Pure mapping adapter converting between immutable domain aggregates and JPA persistence entities.
 * Contains zero business logic, validation, or state transition rules.
 */
@Component
public class WarehouseTransferPersistenceAdapter {

    public WarehouseTransferEntity toEntity(WarehouseTransfer aggregate) {
        WarehouseTransferEntity entity = new WarehouseTransferEntity();
        entity.setId(aggregate.getId());
        entity.setTransferNumber(aggregate.getTransferNumber());
        entity.setTenantId(aggregate.getTenantId());
        entity.setFromWarehouseId(aggregate.getFromWarehouseId());
        entity.setToWarehouseId(aggregate.getToWarehouseId());
        entity.setStatus(aggregate.getState());
        entity.setRequestDate(aggregate.getRequestDate());
        entity.setTotalTransferValue(aggregate.getTotalTransferValue());
        entity.setCreatedBy(aggregate.getCreatedBy());
        entity.setApprovedBy(aggregate.getApprovedBy());
        entity.setShippedBy(aggregate.getShippedBy());
        entity.setReceivedBy(aggregate.getReceivedBy());
        entity.setVersion(aggregate.getVersion());
        entity.setCreatedAt(aggregate.getCreatedAt());
        entity.setUpdatedAt(aggregate.getUpdatedAt());
        entity.setApprovedAt(aggregate.getApprovedAt());
        entity.setShippedAt(aggregate.getShippedAt());
        entity.setReceivedAt(aggregate.getReceivedAt());

        List<WarehouseTransferLineEntity> lineEntities = new ArrayList<>();
        for (WarehouseTransferLine line : aggregate.getLines()) {
            WarehouseTransferLineEntity lineEntity = new WarehouseTransferLineEntity();
            lineEntity.setId(line.getId());
            lineEntity.setProductId(line.getProductId());
            lineEntity.setRequestedQty(line.getRequestedQty());
            lineEntity.setShippedQty(line.getShippedQty());
            lineEntity.setReceivedQty(line.getReceivedQty());
            lineEntity.setDamagedQty(line.getDamagedQty());
            lineEntity.setLotNumber(line.getLotNumber());
            lineEntity.setUnitCost(line.getUnitCost());
            lineEntity.setLineTotal(line.getLineTotal());
            lineEntities.add(lineEntity);
        }
        entity.setLines(lineEntities);
        return entity;
    }

    public WarehouseTransfer toDomain(WarehouseTransferEntity entity) {
        List<WarehouseTransferLine> lines = new ArrayList<>();
        for (WarehouseTransferLineEntity lineEntity : entity.getLines()) {
            lines.add(new WarehouseTransferLine(
                lineEntity.getId(),
                lineEntity.getProductId(),
                lineEntity.getRequestedQty(),
                lineEntity.getShippedQty(),
                lineEntity.getReceivedQty(),
                lineEntity.getDamagedQty(),
                lineEntity.getLotNumber(),
                lineEntity.getUnitCost()
            ));
        }

        return new WarehouseTransfer(
            entity.getId(),
            entity.getTransferNumber(),
            entity.getTenantId(),
            entity.getFromWarehouseId(),
            entity.getToWarehouseId(),
            entity.getStatus(),
            entity.getRequestDate(),
            entity.getTotalTransferValue(),
            entity.getCreatedBy(),
            entity.getApprovedBy(),
            entity.getShippedBy(),
            entity.getReceivedBy(),
            entity.getVersion(),
            entity.getTransferNumber(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getApprovedAt(),
            entity.getShippedAt(),
            entity.getReceivedAt(),
            lines
        );
    }
}
