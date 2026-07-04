package com.spiceflow.backend.sales.delivery.repository;

import com.spiceflow.backend.sales.delivery.entity.DeliveryWorkflowEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for DeliveryWorkflowEntity.
 * Zero business logic leakage: purely CRUD and query methods.
 */
@Repository
public interface DeliveryWorkflowRepository extends JpaRepository<DeliveryWorkflowEntity, Long> {

    Optional<DeliveryWorkflowEntity> findByDeliveryNumberAndTenantId(String deliveryNumber, Long tenantId);

    Optional<DeliveryWorkflowEntity> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByDeliveryNumberAndTenantId(String deliveryNumber, Long tenantId);

    List<DeliveryWorkflowEntity> findAllByTenantId(Long tenantId);

    Page<DeliveryWorkflowEntity> findAllByTenantId(Long tenantId, Pageable pageable);
}
