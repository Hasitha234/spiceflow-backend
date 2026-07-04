package com.spiceflow.backend.sales.collection.adapter;

import com.spiceflow.backend.sales.collection.domain.CashCollection;
import com.spiceflow.backend.sales.collection.entity.CashCollectionWorkflowEntity;
import org.springframework.stereotype.Component;

/**
 * Pure mapping adapter converting between immutable CashCollection domain aggregates
 * and JPA persistence entities.
 * Contains zero business logic, validation, or state transition rules.
 */
@Component
public class CashCollectionPersistenceAdapter {

    public CashCollectionWorkflowEntity toEntity(CashCollection aggregate) {
        CashCollectionWorkflowEntity entity = new CashCollectionWorkflowEntity();
        entity.setId(aggregate.getId());
        entity.setCollectionNumber(aggregate.getCollectionNumber());
        entity.setCorrelationId(aggregate.getCorrelationId());
        entity.setTenantId(aggregate.getTenantId());
        entity.setShopId(aggregate.getShopId());
        entity.setRepId(aggregate.getRepId());
        entity.setCollectionDate(aggregate.getCollectionDate());
        entity.setAmount(aggregate.getAmount());
        entity.setPaymentMethod(aggregate.getPaymentMethod());
        entity.setChequeNo(aggregate.getChequeNo());
        entity.setChequeBankName(aggregate.getChequeBankName());
        entity.setChequeDate(aggregate.getChequeDate());
        entity.setNotes(aggregate.getNotes());
        entity.setStatus(aggregate.getState());
        entity.setCreatedBy(aggregate.getCreatedBy());
        entity.setConfirmedBy(aggregate.getConfirmedBy());
        entity.setCancelledBy(aggregate.getCancelledBy());
        entity.setVersion(aggregate.getVersion());
        entity.setCreatedAt(aggregate.getCreatedAt());
        entity.setUpdatedAt(aggregate.getUpdatedAt());
        entity.setConfirmedAt(aggregate.getConfirmedAt());
        entity.setCancelledAt(aggregate.getCancelledAt());
        return entity;
    }

    public CashCollection toAggregate(CashCollectionWorkflowEntity entity) {
        return new CashCollection(
                entity.getId(),
                entity.getCollectionNumber(),
                entity.getTenantId(),
                entity.getShopId(),
                entity.getRepId(),
                entity.getCollectionDate(),
                entity.getAmount(),
                entity.getPaymentMethod(),
                entity.getChequeNo(),
                entity.getChequeBankName(),
                entity.getChequeDate(),
                entity.getNotes(),
                entity.getStatus(),
                entity.getCreatedBy(),
                entity.getConfirmedBy(),
                entity.getCancelledBy(),
                entity.getVersion(),
                entity.getCorrelationId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getConfirmedAt(),
                entity.getCancelledAt()
        );
    }
}
