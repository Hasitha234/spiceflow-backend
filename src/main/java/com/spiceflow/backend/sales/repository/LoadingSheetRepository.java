package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.LoadingSheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface LoadingSheetRepository extends JpaRepository<LoadingSheet, Long> {
    @EntityGraph(attributePaths = {"repOrder", "repOrder.rep", "driver"})
    Optional<LoadingSheet> findByIdAndTenantId(Long id, Long tenantId);

    @EntityGraph(attributePaths = {"repOrder", "repOrder.rep", "driver"})
    Page<LoadingSheet> findByTenantId(Long tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"repOrder", "repOrder.rep", "driver"})
    Page<LoadingSheet> findByTenantIdAndDriverId(Long tenantId, Long driverId, Pageable pageable);

    @EntityGraph(attributePaths = {"repOrder", "repOrder.rep", "driver"})
    Page<LoadingSheet> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"repOrder", "repOrder.rep", "driver"})
    Page<LoadingSheet> findByTenantIdAndDriverIdAndStatus(Long tenantId, Long driverId, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"repOrder", "repOrder.rep", "driver"})
    Page<LoadingSheet> findByTenantIdAndDriverIdAndStatusIn(Long tenantId, Long driverId, java.util.List<String> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"repOrder", "repOrder.rep", "driver"})
    java.util.List<LoadingSheet> findByTenantIdAndLoadingDateAndStatus(Long tenantId, java.time.LocalDate loadingDate, String status);
}
