package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.CancelSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

import java.util.List;

@Repository
public interface CancelSummaryRepository extends JpaRepository<CancelSummary, Long> {

    Optional<CancelSummary> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT cs FROM CancelSummary cs WHERE cs.tenant.id = :tenantId " +
           "AND (:search IS NULL OR cs.summaryNumber LIKE %:search%) " +
           "AND (:repId IS NULL OR cs.rep.id = :repId) " +
           "AND (:driverId IS NULL OR cs.driver.id = :driverId) " +
           "AND (cast(:startDate as date) IS NULL OR cs.summaryDate >= :startDate) " +
           "AND (cast(:endDate as date) IS NULL OR cs.summaryDate <= :endDate) " +
           "AND (:status IS NULL OR cs.status = :status)")
    Page<CancelSummary> findByFilters(
        @Param("tenantId") Long tenantId,
        @Param("search") String search,
        @Param("repId") Long repId,
        @Param("driverId") Long driverId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") String status,
        Pageable pageable
    );

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(summary_number, 13) AS INTEGER)), 0) " +
           "FROM cancel_summaries " +
           "WHERE tenant_id = :tenantId " +
           "AND summary_date = :date " +
           "AND summary_number LIKE 'CS-%'", nativeQuery = true)
    int findMaxSequenceNumberForDate(@Param("tenantId") Long tenantId, @Param("date") LocalDate date);

    List<CancelSummary> findByTenantIdAndRepIdAndSummaryDate(Long tenantId, Long repId, LocalDate summaryDate);
}
