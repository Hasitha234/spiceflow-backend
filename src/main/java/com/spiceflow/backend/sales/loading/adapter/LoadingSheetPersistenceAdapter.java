package com.spiceflow.backend.sales.loading.adapter;

import com.spiceflow.backend.sales.loading.domain.LoadingSheet;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetItem;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetReturnItem;
import com.spiceflow.backend.sales.loading.entity.LoadingSheetItemWorkflowEntity;
import com.spiceflow.backend.sales.loading.entity.LoadingSheetReturnWorkflowEntity;
import com.spiceflow.backend.sales.loading.entity.LoadingSheetWorkflowEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Pure mapping adapter converting between immutable domain aggregates and JPA persistence entities.
 * Contains zero business logic, validation, or state transition rules.
 */
@Component
public class LoadingSheetPersistenceAdapter {

    public LoadingSheetWorkflowEntity toEntity(LoadingSheet aggregate) {
        LoadingSheetWorkflowEntity entity = new LoadingSheetWorkflowEntity();
        entity.setId(aggregate.getId());
        entity.setSheetNumber(aggregate.getSheetNumber());
        entity.setTenantId(aggregate.getTenantId());
        entity.setRepOrderId(aggregate.getRepOrderId());
        entity.setDriverId(aggregate.getDriverId());
        entity.setLoadingDate(aggregate.getLoadingDate());
        entity.setStatus(aggregate.getState());
        entity.setCreatedBy(aggregate.getCreatedBy());
        entity.setConfirmedBy(aggregate.getConfirmedBy());
        entity.setCancelledBy(aggregate.getCancelledBy());
        entity.setVersion(aggregate.getVersion());
        entity.setCreatedAt(aggregate.getCreatedAt());
        entity.setUpdatedAt(aggregate.getUpdatedAt());
        entity.setConfirmedAt(aggregate.getConfirmedAt());
        entity.setCancelledAt(aggregate.getCancelledAt());

        List<LoadingSheetItemWorkflowEntity> itemEntities = new ArrayList<>();
        for (LoadingSheetItem item : aggregate.getItems()) {
            LoadingSheetItemWorkflowEntity itemEntity = new LoadingSheetItemWorkflowEntity();
            itemEntity.setId(item.id());
            itemEntity.setLoadingSheet(entity);
            itemEntity.setTenantId(aggregate.getTenantId());
            itemEntity.setProductId(item.productId());
            itemEntity.setQuantityLoaded(item.quantityLoaded());
            itemEntity.setUnitType(item.unitType());
            itemEntities.add(itemEntity);
        }
        entity.setItems(itemEntities);

        List<LoadingSheetReturnWorkflowEntity> returnEntities = new ArrayList<>();
        for (LoadingSheetReturnItem returnItem : aggregate.getReturns()) {
            LoadingSheetReturnWorkflowEntity returnEntity = new LoadingSheetReturnWorkflowEntity();
            returnEntity.setId(returnItem.id());
            returnEntity.setLoadingSheet(entity);
            returnEntity.setTenantId(aggregate.getTenantId());
            returnEntity.setProductId(returnItem.productId());
            returnEntity.setQuantityReturned(returnItem.quantityReturned());
            returnEntity.setUnitType(returnItem.unitType());
            returnEntity.setReturnType(returnItem.returnType());
            returnEntities.add(returnEntity);
        }
        entity.setReturns(returnEntities);

        return entity;
    }

    public LoadingSheet toDomain(LoadingSheetWorkflowEntity entity) {
        List<LoadingSheetItem> items = new ArrayList<>();
        for (LoadingSheetItemWorkflowEntity itemEntity : entity.getItems()) {
            items.add(new LoadingSheetItem(
                itemEntity.getId(),
                itemEntity.getProductId(),
                itemEntity.getQuantityLoaded(),
                itemEntity.getUnitType()
            ));
        }

        List<LoadingSheetReturnItem> returns = new ArrayList<>();
        for (LoadingSheetReturnWorkflowEntity returnEntity : entity.getReturns()) {
            returns.add(new LoadingSheetReturnItem(
                returnEntity.getId(),
                returnEntity.getProductId(),
                returnEntity.getQuantityReturned(),
                returnEntity.getUnitType(),
                returnEntity.getReturnType()
            ));
        }

        return new LoadingSheet(
            entity.getId(),
            entity.getSheetNumber() != null ? entity.getSheetNumber() : "",
            entity.getTenantId(),
            entity.getRepOrderId(),
            "",
            entity.getDriverId(),
            "",
            entity.getLoadingDate(),
            entity.getStatus(),
            entity.getCreatedBy() != null ? entity.getCreatedBy() : "",
            entity.getConfirmedBy(),
            entity.getCancelledBy(),
            entity.getVersion(),
            entity.getSheetNumber() != null ? entity.getSheetNumber() : "",
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getConfirmedAt(),
            entity.getCancelledAt(),
            items,
            returns
        );
    }
}
