package com.spiceflow.backend.sales.order.adapter;

import com.spiceflow.backend.sales.order.domain.RepOrder;
import com.spiceflow.backend.sales.order.domain.RepOrderItem;
import com.spiceflow.backend.sales.order.domain.RepOrderShop;
import com.spiceflow.backend.sales.order.domain.ShopReturnItem;
import com.spiceflow.backend.sales.order.entity.RepOrderItemWorkflowEntity;
import com.spiceflow.backend.sales.order.entity.RepOrderShopWorkflowEntity;
import com.spiceflow.backend.sales.order.entity.RepOrderWorkflowEntity;
import com.spiceflow.backend.sales.order.entity.ShopReturnWorkflowEntity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Pure mapping adapter converting between immutable domain aggregates and JPA persistence entities.
 * Contains zero business logic, validation, or state transition rules.
 */
@Component
public class RepOrderPersistenceAdapter {

    public RepOrderWorkflowEntity toEntity(RepOrder aggregate) {
        RepOrderWorkflowEntity entity = new RepOrderWorkflowEntity();
        entity.setId(aggregate.getId());
        entity.setOrderNumber(aggregate.getOrderNumber());
        entity.setTenantId(aggregate.getTenantId());
        entity.setRepId(aggregate.getRepId());
        entity.setOrderDate(aggregate.getOrderDate());
        entity.setRouteArea(aggregate.getRouteArea());
        entity.setStatus(aggregate.getState());
        entity.setTotalGrossAmount(aggregate.getTotalGrossAmount());
        entity.setTotalReturnsValue(aggregate.getTotalReturnsValue());
        entity.setNetAmount(aggregate.getNetAmount());
        entity.setLoadingStatus(aggregate.getState().name());
        entity.setCreatedBy(aggregate.getCreatedBy());
        entity.setApprovedBy(aggregate.getApprovedBy());
        entity.setLoadedBy(aggregate.getLoadedBy());
        entity.setDeliveredBy(aggregate.getDeliveredBy());
        entity.setVersion(aggregate.getVersion());
        entity.setCreatedAt(aggregate.getCreatedAt());
        entity.setUpdatedAt(aggregate.getUpdatedAt());
        entity.setApprovedAt(aggregate.getApprovedAt());
        entity.setLoadedAt(aggregate.getLoadedAt());
        entity.setDeliveredAt(aggregate.getDeliveredAt());

        List<RepOrderShopWorkflowEntity> shopEntities = new ArrayList<>();
        for (RepOrderShop shop : aggregate.getShops()) {
            RepOrderShopWorkflowEntity shopEntity = new RepOrderShopWorkflowEntity();
            shopEntity.setId(shop.id());
            shopEntity.setTenantId(aggregate.getTenantId());
            shopEntity.setShopId(shop.shopId());
            shopEntity.setGrossOrderAmount(shop.grossOrderAmount() != null ? shop.grossOrderAmount() : BigDecimal.ZERO);
            shopEntity.setReturnsValue(shop.returnsValue() != null ? shop.returnsValue() : BigDecimal.ZERO);
            shopEntity.setNetAmount(shop.netAmount() != null ? shop.netAmount() : BigDecimal.ZERO);
            shopEntity.setCreatedAt(aggregate.getCreatedAt());

            List<RepOrderItemWorkflowEntity> itemEntities = new ArrayList<>();
            for (RepOrderItem item : shop.items()) {
                RepOrderItemWorkflowEntity itemEntity = new RepOrderItemWorkflowEntity();
                itemEntity.setId(item.id());
                itemEntity.setTenantId(aggregate.getTenantId());
                itemEntity.setProductId(item.productId());
                itemEntity.setQuantity(item.quantity());
                itemEntity.setUnitType(item.unitType());
                itemEntity.setRate(item.rate());
                itemEntity.setGrossAmount(item.grossAmount() != null ? item.grossAmount() : BigDecimal.ZERO);
                itemEntity.setNetAmount(item.netAmount() != null ? item.netAmount() : BigDecimal.ZERO);
                itemEntity.setIsFreeItem(item.isFreeItem());
                itemEntity.setBoxesNeeded(item.boxesNeeded());
                itemEntity.setCreatedAt(aggregate.getCreatedAt());
                itemEntities.add(itemEntity);
            }
            shopEntity.setItems(itemEntities);

            List<ShopReturnWorkflowEntity> returnEntities = new ArrayList<>();
            for (ShopReturnItem r : shop.returns()) {
                ShopReturnWorkflowEntity returnEntity = new ShopReturnWorkflowEntity();
                returnEntity.setId(r.id());
                returnEntity.setTenantId(aggregate.getTenantId());
                returnEntity.setProductId(r.productId());
                returnEntity.setQuantity(r.quantity());
                returnEntity.setUnitType(r.unitType());
                returnEntity.setCreditValue(r.creditValue());
                returnEntity.setReturnType(r.returnType());
                returnEntity.setStatus(r.status() != null ? r.status() : "PENDING");
                returnEntity.setCreatedAt(aggregate.getCreatedAt());
                returnEntities.add(returnEntity);
            }
            shopEntity.setReturns(returnEntities);

            shopEntities.add(shopEntity);
        }
        entity.setShops(shopEntities);
        return entity;
    }

    public RepOrder toDomain(RepOrderWorkflowEntity entity) {
        List<RepOrderShop> shops = new ArrayList<>();
        for (RepOrderShopWorkflowEntity shopEntity : entity.getShops()) {
            List<RepOrderItem> items = new ArrayList<>();
            for (RepOrderItemWorkflowEntity itemEntity : shopEntity.getItems()) {
                items.add(new RepOrderItem(
                    itemEntity.getId(),
                    itemEntity.getProductId(),
                    itemEntity.getQuantity(),
                    itemEntity.getUnitType(),
                    itemEntity.getRate(),
                    itemEntity.getGrossAmount(),
                    itemEntity.getNetAmount(),
                    itemEntity.getIsFreeItem(),
                    itemEntity.getBoxesNeeded()
                ));
            }

            List<ShopReturnItem> returns = new ArrayList<>();
            for (ShopReturnWorkflowEntity returnEntity : shopEntity.getReturns()) {
                returns.add(new ShopReturnItem(
                    returnEntity.getId(),
                    returnEntity.getProductId(),
                    returnEntity.getQuantity(),
                    returnEntity.getUnitType(),
                    returnEntity.getCreditValue(),
                    returnEntity.getReturnType(),
                    returnEntity.getStatus()
                ));
            }

            shops.add(new RepOrderShop(
                shopEntity.getId(),
                shopEntity.getShopId(),
                shopEntity.getGrossOrderAmount(),
                shopEntity.getReturnsValue(),
                shopEntity.getNetAmount(),
                items,
                returns
            ));
        }

        return new RepOrder(
            entity.getId(),
            entity.getOrderNumber() != null ? entity.getOrderNumber() : "",
            entity.getTenantId(),
            entity.getRepId(),
            entity.getOrderDate(),
            entity.getRouteArea() != null ? entity.getRouteArea() : "",
            entity.getStatus(),
            entity.getTotalGrossAmount(),
            entity.getTotalReturnsValue(),
            entity.getNetAmount(),
            entity.getCreatedBy() != null ? entity.getCreatedBy() : "",
            entity.getApprovedBy(),
            entity.getLoadedBy(),
            entity.getDeliveredBy(),
            entity.getVersion(),
            entity.getOrderNumber() != null ? entity.getOrderNumber() : "",
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getApprovedAt(),
            entity.getLoadedAt(),
            entity.getDeliveredAt(),
            shops
        );
    }
}
