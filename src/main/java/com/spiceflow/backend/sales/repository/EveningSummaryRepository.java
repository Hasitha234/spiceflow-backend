package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.EveningSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface EveningSummaryRepository extends JpaRepository<EveningSummary, Long> {

    Optional<EveningSummary> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT es FROM EveningSummary es WHERE es.tenant.id = :tenantId " +
           "AND (cast(:search as text) IS NULL OR LOWER(es.summaryNumber) LIKE LOWER(CONCAT('%', cast(:search as text), '%'))) " +
           "AND (cast(:repId as long) IS NULL OR es.rep.id = :repId) " +
           "AND (cast(:driverId as long) IS NULL OR es.driver.id = :driverId) " +
           "AND (cast(:startDate as date) IS NULL OR es.summaryDate >= :startDate) " +
           "AND (cast(:endDate as date) IS NULL OR es.summaryDate <= :endDate) " +
           "AND (cast(:status as text) IS NULL OR es.status = :status) " +
           "ORDER BY es.summaryDate DESC")
    Page<EveningSummary> findByFilters(
        @Param("tenantId") Long tenantId,
        @Nullable @Param("search") String search,
        @Nullable @Param("repId") Long repId,
        @Nullable @Param("driverId") Long driverId,
        @Nullable @Param("startDate") LocalDate startDate,
        @Nullable @Param("endDate") LocalDate endDate,
        @Nullable @Param("status") String status,
        Pageable pageable
    );

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(es.summary_number, 13) AS INTEGER)), 0) " +
           "FROM evening_summaries es " +
           "WHERE es.id IN (" +
           "  SELECT id FROM evening_summaries " +
           "  WHERE tenant_id = :tenantId " +
           "  AND summary_date = :date " +
           "  AND summary_number LIKE 'ES-%' " +
           "  AND deleted_at IS NULL " +
           "  FOR UPDATE" +
           ")", nativeQuery = true)
    int findMaxSequenceNumberForDate(@Param("tenantId") Long tenantId, @Param("date") LocalDate date);

    List<EveningSummary> findByTenantIdAndSummaryDate(Long tenantId, LocalDate summaryDate);
}
