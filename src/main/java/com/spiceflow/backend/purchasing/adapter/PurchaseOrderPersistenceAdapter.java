package com.spiceflow.backend.purchasing.adapter;

import com.spiceflow.backend.purchasing.domain.PurchaseOrder;
import com.spiceflow.backend.purchasing.domain.PurchaseOrderLine;
import com.spiceflow.backend.purchasing.entity.PurchaseOrderEntity;
import com.spiceflow.backend.purchasing.entity.PurchaseOrderLineEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Pure mapping adapter converting between immutable domain aggregates and JPA persistence entities.
 * Contains zero business logic, validation, or state transition rules.
 */
@Component
public class PurchaseOrderPersistenceAdapter {

    public PurchaseOrderEntity toEntity(PurchaseOrder aggregate) {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        entity.setId(aggregate.getId());
        entity.setCorrelationId(aggregate.getCorrelationId());
        entity.setTenantId(aggregate.getTenantId());
        entity.setSupplierId(aggregate.getSupplierId());
        entity.setStatus(aggregate.getState());
        entity.setVersion(aggregate.getVersion());
        entity.setOrderDate(aggregate.getOrderDate());
        entity.setTotalAmount(aggregate.getTotalAmount());
        entity.setCreatedBy(aggregate.getCreatedBy());
        entity.setCreatedAt(aggregate.getCreatedAt());
        entity.setUpdatedAt(aggregate.getUpdatedAt());
        entity.setSubmittedAt(aggregate.getSubmittedAt());
        entity.setReceivedAt(aggregate.getReceivedAt());

        List<PurchaseOrderLineEntity> lineEntities = new ArrayList<>();
        for (PurchaseOrderLine line : aggregate.getLines()) {
            PurchaseOrderLineEntity lineEntity = new PurchaseOrderLineEntity();
            lineEntity.setId(line.getId());
            lineEntity.setProductId(line.getProductId());
            lineEntity.setQuantity(line.getQuantity());
            lineEntity.setUnitPrice(line.getUnitPrice());
            lineEntity.setLineTotal(line.getLineTotal());
            lineEntities.add(lineEntity);
        }
        entity.setLines(lineEntities);
        return entity;
    }

    public PurchaseOrder toDomain(PurchaseOrderEntity entity) {
        List<PurchaseOrderLine> lines = new ArrayList<>();
        for (PurchaseOrderLineEntity lineEntity : entity.getLines()) {
            lines.add(new PurchaseOrderLine(
                lineEntity.getId(),
                lineEntity.getProductId(),
                lineEntity.getQuantity(),
                lineEntity.getUnitPrice()
            ));
        }

        return new PurchaseOrder(
            entity.getId(),
            entity.getCorrelationId(), // poNumber maps to correlationId in DB schema
            entity.getTenantId(),
            entity.getSupplierId(),
            entity.getStatus(),
            entity.getOrderDate(),
            entity.getTotalAmount(),
            entity.getCreatedBy(),
            entity.getVersion(),
            entity.getCorrelationId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getSubmittedAt(),
            entity.getReceivedAt(),
            lines
        );
    }
}
