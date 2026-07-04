package com.spiceflow.backend.sales.loading.repository;

import com.spiceflow.backend.sales.loading.entity.LoadingSheetWorkflowEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for LoadingSheetWorkflowEntity.
 * Zero business logic leakage: purely CRUD and query methods.
 */
@Repository
public interface LoadingSheetWorkflowRepository extends JpaRepository<LoadingSheetWorkflowEntity, Long> {

    Optional<LoadingSheetWorkflowEntity> findBySheetNumberAndTenantId(String sheetNumber, Long tenantId);

    Optional<LoadingSheetWorkflowEntity> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsBySheetNumberAndTenantId(String sheetNumber, Long tenantId);
}
