package com.spiceflow.backend.sales.delivery.adapter;

import com.spiceflow.backend.sales.delivery.domain.Delivery;
import com.spiceflow.backend.sales.delivery.domain.DeliveryPaymentRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryReturnItemRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryShopItemRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryShopRecord;
import com.spiceflow.backend.sales.delivery.entity.DeliveryPaymentWorkflowEntity;
import com.spiceflow.backend.sales.delivery.entity.DeliveryShopItemWorkflowEntity;
import com.spiceflow.backend.sales.delivery.entity.DeliveryShopReturnWorkflowEntity;
import com.spiceflow.backend.sales.delivery.entity.DeliveryShopWorkflowEntity;
import com.spiceflow.backend.sales.delivery.entity.DeliveryWorkflowEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Pure mapping adapter converting between immutable Delivery domain aggregates
 * and JPA persistence entities.
 * Contains zero business logic, validation, or state transition rules.
 */
@Component
public class DeliveryPersistenceAdapter {

    public DeliveryWorkflowEntity toEntity(Delivery aggregate) {
        DeliveryWorkflowEntity entity = new DeliveryWorkflowEntity();
        entity.setId(aggregate.getId());
        entity.setDeliveryNumber(aggregate.getDeliveryNumber());
        entity.setTenantId(aggregate.getTenantId());
        entity.setLoadingSheetId(aggregate.getLoadingSheetId());
        entity.setDeliveryDate(aggregate.getDeliveryDate());
        entity.setStatus(aggregate.getState());
        entity.setTotalSalesValue(aggregate.getTotalSalesValue());
        entity.setTotalReturnsValue(aggregate.getTotalReturnsValue());
        entity.setTotalCollectedAmount(aggregate.getTotalCollectedAmount());
        entity.setCreatedBy(aggregate.getCreatedBy());
        entity.setDispatchedBy(aggregate.getDispatchedBy());
        entity.setCompletedBy(aggregate.getCompletedBy());
        entity.setCancelledBy(aggregate.getCancelledBy());
        entity.setVersion(aggregate.getVersion());
        entity.setCreatedAt(aggregate.getCreatedAt());
        entity.setUpdatedAt(aggregate.getUpdatedAt());
        entity.setDispatchedAt(aggregate.getDispatchedAt());
        entity.setCompletedAt(aggregate.getCompletedAt());
        entity.setCancelledAt(aggregate.getCancelledAt());

        List<DeliveryShopWorkflowEntity> shopEntities = new ArrayList<>();
        for (DeliveryShopRecord shop : aggregate.getShops()) {
            DeliveryShopWorkflowEntity shopEntity = new DeliveryShopWorkflowEntity();
            shopEntity.setId(shop.id());
            shopEntity.setDelivery(entity);
            shopEntity.setTenantId(aggregate.getTenantId());
            shopEntity.setShopId(shop.shopId());
            shopEntity.setGrossBillAmount(shop.grossBillAmount());
            shopEntity.setTotalDiscount(shop.totalDiscount());
            shopEntity.setReturnsDeducted(shop.returnsDeducted());
            shopEntity.setNetPayable(shop.netPayable());
            shopEntity.setPaidAmount(shop.paidAmount());
            shopEntity.setCreditAmount(shop.creditAmount());

            // Items
            List<DeliveryShopItemWorkflowEntity> itemEntities = new ArrayList<>();
            for (DeliveryShopItemRecord item : shop.items()) {
                DeliveryShopItemWorkflowEntity itemEntity = new DeliveryShopItemWorkflowEntity();
                itemEntity.setId(item.id());
                itemEntity.setDeliveryShop(shopEntity);
                itemEntity.setTenantId(aggregate.getTenantId());
                itemEntity.setProductId(item.productId());
                itemEntity.setQuantityDelivered(item.quantityDelivered());
                itemEntity.setUnitType(item.unitType());
                itemEntity.setRate(item.rate());
                itemEntity.setGrossAmount(item.grossAmount());
                itemEntity.setDiscountAmount(item.discountAmount());
                itemEntity.setNetAmount(item.netAmount());
                itemEntity.setFreeItem(item.isFreeItem());
                itemEntities.add(itemEntity);
            }
            shopEntity.setItems(itemEntities);

            // Returns
            List<DeliveryShopReturnWorkflowEntity> returnEntities = new ArrayList<>();
            for (DeliveryReturnItemRecord ret : shop.returns()) {
                DeliveryShopReturnWorkflowEntity returnEntity = new DeliveryShopReturnWorkflowEntity();
                returnEntity.setId(ret.id());
                returnEntity.setDeliveryShop(shopEntity);
                returnEntity.setTenantId(aggregate.getTenantId());
                returnEntity.setProductId(ret.productId());
                returnEntity.setQuantityReturned(ret.quantityReturned());
                returnEntity.setUnitType(ret.unitType());
                returnEntity.setCreditValue(ret.creditValue());
                returnEntity.setReturnType(ret.returnType());
                returnEntities.add(returnEntity);
            }
            shopEntity.setReturns(returnEntities);

            // Payments
            List<DeliveryPaymentWorkflowEntity> paymentEntities = new ArrayList<>();
            for (DeliveryPaymentRecord payment : shop.payments()) {
                DeliveryPaymentWorkflowEntity paymentEntity = new DeliveryPaymentWorkflowEntity();
                paymentEntity.setId(payment.id());
                paymentEntity.setDeliveryShop(shopEntity);
                paymentEntity.setTenantId(aggregate.getTenantId());
                paymentEntity.setPaymentMethod(payment.paymentMethod());
                paymentEntity.setAmount(payment.amount());
                paymentEntity.setChequeNo(payment.chequeNo());
                paymentEntity.setChequeBankName(payment.chequeBankName());
                paymentEntity.setChequeDate(payment.chequeDate());
                paymentEntities.add(paymentEntity);
            }
            shopEntity.setPayments(paymentEntities);

            shopEntities.add(shopEntity);
        }
        entity.setShops(shopEntities);

        return entity;
    }

    public Delivery toDomain(DeliveryWorkflowEntity entity) {
        List<DeliveryShopRecord> shops = new ArrayList<>();
        for (DeliveryShopWorkflowEntity shopEntity : entity.getShops()) {
            List<DeliveryShopItemRecord> items = new ArrayList<>();
            for (DeliveryShopItemWorkflowEntity itemEntity : shopEntity.getItems()) {
                items.add(new DeliveryShopItemRecord(
                        itemEntity.getId(),
                        itemEntity.getProductId(),
                        itemEntity.getQuantityDelivered(),
                        itemEntity.getUnitType(),
                        itemEntity.getRate(),
                        itemEntity.getGrossAmount(),
                        itemEntity.getDiscountAmount(),
                        itemEntity.getNetAmount(),
                        itemEntity.isFreeItem()
                ));
            }

            List<DeliveryReturnItemRecord> returns = new ArrayList<>();
            for (DeliveryShopReturnWorkflowEntity returnEntity : shopEntity.getReturns()) {
                returns.add(new DeliveryReturnItemRecord(
                        returnEntity.getId(),
                        returnEntity.getProductId(),
                        returnEntity.getQuantityReturned(),
                        returnEntity.getUnitType(),
                        returnEntity.getCreditValue(),
                        returnEntity.getReturnType()
                ));
            }

            List<DeliveryPaymentRecord> payments = new ArrayList<>();
            for (DeliveryPaymentWorkflowEntity paymentEntity : shopEntity.getPayments()) {
                payments.add(new DeliveryPaymentRecord(
                        paymentEntity.getId(),
                        paymentEntity.getPaymentMethod(),
                        paymentEntity.getAmount(),
                        paymentEntity.getChequeNo(),
                        paymentEntity.getChequeBankName(),
                        paymentEntity.getChequeDate()
                ));
            }

            shops.add(new DeliveryShopRecord(
                    shopEntity.getId(),
                    shopEntity.getShopId(),
                    shopEntity.getGrossBillAmount(),
                    shopEntity.getTotalDiscount(),
                    shopEntity.getReturnsDeducted(),
                    shopEntity.getNetPayable(),
                    shopEntity.getPaidAmount(),
                    shopEntity.getCreditAmount(),
                    items,
                    returns,
                    payments
            ));
        }

        return new Delivery(
                entity.getId(),
                entity.getDeliveryNumber() != null ? entity.getDeliveryNumber() : "",
                entity.getTenantId(),
                entity.getLoadingSheetId(),
                entity.getDeliveryNumber(),
                entity.getDeliveryDate(),
                entity.getStatus(),
                entity.getTotalSalesValue(),
                entity.getTotalReturnsValue(),
                entity.getTotalCollectedAmount(),
                entity.getCreatedBy() != null ? entity.getCreatedBy() : "",
                entity.getDispatchedBy(),
                entity.getCompletedBy(),
                entity.getCancelledBy(),
                entity.getVersion(),
                entity.getDeliveryNumber() != null ? entity.getDeliveryNumber() : "",
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDispatchedAt(),
                entity.getCompletedAt(),
                entity.getCancelledAt(),
                shops
        );
    }
}
