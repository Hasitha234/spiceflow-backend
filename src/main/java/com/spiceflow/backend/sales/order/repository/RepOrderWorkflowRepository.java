package com.spiceflow.backend.sales.order.repository;

import com.spiceflow.backend.sales.order.entity.RepOrderWorkflowEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for RepOrderWorkflowEntity.
 * Zero business logic leakage: purely CRUD and query methods.
 */
@Repository
public interface RepOrderWorkflowRepository extends JpaRepository<RepOrderWorkflowEntity, Long> {

    Optional<RepOrderWorkflowEntity> findByOrderNumberAndTenantId(String orderNumber, Long tenantId);

    Optional<RepOrderWorkflowEntity> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByOrderNumberAndTenantId(String orderNumber, Long tenantId);
}
