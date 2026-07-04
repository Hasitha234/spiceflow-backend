package com.spiceflow.backend.sales.collection.repository;

import com.spiceflow.backend.sales.collection.entity.CashCollectionWorkflowEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for CashCollectionWorkflowEntity.
 * Zero business logic leakage: purely CRUD and query methods.
 */
@Repository
public interface CashCollectionWorkflowRepository extends JpaRepository<CashCollectionWorkflowEntity, Long> {

    Optional<CashCollectionWorkflowEntity> findByCollectionNumberAndTenantId(String collectionNumber, Long tenantId);

    Optional<CashCollectionWorkflowEntity> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByCollectionNumberAndTenantId(String collectionNumber, Long tenantId);

    List<CashCollectionWorkflowEntity> findAllByTenantId(Long tenantId);

    Page<CashCollectionWorkflowEntity> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<CashCollectionWorkflowEntity> findAllByTenantIdAndShopId(Long tenantId, Long shopId, Pageable pageable);
}
