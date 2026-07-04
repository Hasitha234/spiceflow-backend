package com.spiceflow.backend.receiving.adapter;

import com.spiceflow.backend.receiving.domain.GoodsReceipt;
import com.spiceflow.backend.receiving.domain.GoodsReceiptLine;
import com.spiceflow.backend.receiving.entity.GoodsReceiptEntity;
import com.spiceflow.backend.receiving.entity.GoodsReceiptLineEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Pure mapping adapter converting between immutable domain aggregates and JPA persistence entities.
 * Contains zero business logic, validation, or state transition rules.
 */
@Component
public class GoodsReceiptPersistenceAdapter {

    public GoodsReceiptEntity toEntity(GoodsReceipt aggregate) {
        GoodsReceiptEntity entity = new GoodsReceiptEntity();
        entity.setId(aggregate.getId());
        entity.setReceiptNumber(aggregate.getReceiptNumber());
        entity.setTenantId(aggregate.getTenantId());
        entity.setPurchaseOrderId(aggregate.getPurchaseOrderId());
        entity.setPoNumber(aggregate.getPoNumber());
        entity.setSupplierId(aggregate.getSupplierId());
        entity.setWarehouseId(aggregate.getWarehouseId());
        entity.setStatus(aggregate.getState());
        entity.setReceiptDate(aggregate.getReceiptDate());
        entity.setTotalAcceptedValue(aggregate.getTotalAcceptedValue());
        entity.setTotalDamagedValue(aggregate.getTotalDamagedValue());
        entity.setCreatedBy(aggregate.getCreatedBy());
        entity.setVerifiedBy(aggregate.getVerifiedBy());
        entity.setPostedBy(aggregate.getPostedBy());
        entity.setVersion(aggregate.getVersion());
        entity.setCreatedAt(aggregate.getCreatedAt());
        entity.setUpdatedAt(aggregate.getUpdatedAt());
        entity.setVerifiedAt(aggregate.getVerifiedAt());
        entity.setPostedAt(aggregate.getPostedAt());

        List<GoodsReceiptLineEntity> lineEntities = new ArrayList<>();
        for (GoodsReceiptLine line : aggregate.getLines()) {
            GoodsReceiptLineEntity lineEntity = new GoodsReceiptLineEntity();
            lineEntity.setId(line.getId());
            lineEntity.setProductId(line.getProductId());
            lineEntity.setExpectedQty(line.getExpectedQty());
            lineEntity.setReceivedQty(line.getReceivedQty());
            lineEntity.setAcceptedQty(line.getAcceptedQty());
            lineEntity.setDamagedQty(line.getDamagedQty());
            lineEntity.setLotNumber(line.getLotNumber());
            lineEntity.setExpirationDate(line.getExpirationDate());
            lineEntity.setUnitPrice(line.getUnitPrice());
            lineEntity.setLineTotal(line.getLineTotal());
            lineEntities.add(lineEntity);
        }
        entity.setLines(lineEntities);
        return entity;
    }

    public GoodsReceipt toDomain(GoodsReceiptEntity entity) {
        List<GoodsReceiptLine> lines = new ArrayList<>();
        for (GoodsReceiptLineEntity lineEntity : entity.getLines()) {
            lines.add(new GoodsReceiptLine(
                lineEntity.getId(),
                lineEntity.getProductId(),
                lineEntity.getExpectedQty(),
                lineEntity.getReceivedQty(),
                lineEntity.getAcceptedQty(),
                lineEntity.getDamagedQty(),
                lineEntity.getLotNumber(),
                lineEntity.getExpirationDate(),
                lineEntity.getUnitPrice()
            ));
        }

        return new GoodsReceipt(
            entity.getId(),
            entity.getReceiptNumber(),
            entity.getTenantId(),
            entity.getPurchaseOrderId(),
            entity.getPoNumber(),
            entity.getSupplierId(),
            entity.getWarehouseId(),
            entity.getStatus(),
            entity.getReceiptDate(),
            entity.getTotalAcceptedValue(),
            entity.getTotalDamagedValue(),
            entity.getCreatedBy(),
            entity.getVerifiedBy(),
            entity.getPostedBy(),
            entity.getVersion(),
            entity.getReceiptNumber(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVerifiedAt(),
            entity.getPostedAt(),
            lines
        );
    }
}
