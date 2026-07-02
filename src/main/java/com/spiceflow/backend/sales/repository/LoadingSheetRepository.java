package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.LoadingSheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoadingSheetRepository extends JpaRepository<LoadingSheet, Long> {
    Optional<LoadingSheet> findByIdAndTenantId(Long id, Long tenantId);
    Page<LoadingSheet> findByTenantId(Long tenantId, Pageable pageable);
}
