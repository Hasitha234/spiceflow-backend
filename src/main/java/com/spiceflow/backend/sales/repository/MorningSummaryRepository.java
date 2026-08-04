package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.MorningSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

import java.util.List;

@Repository
public interface MorningSummaryRepository extends JpaRepository<MorningSummary, Long> {
    Optional<MorningSummary> findByIdAndTenantId(Long id, Long tenantId);
    Page<MorningSummary> findByTenantId(Long tenantId, Pageable pageable);
    
    // Check if summary number exists
    boolean existsBySummaryNumberAndTenantId(String summaryNumber, Long tenantId);
    
    // For auto-generating numbers, get the latest
    Optional<MorningSummary> findFirstByTenantIdOrderByCreatedAtDesc(Long tenantId);
    
    List<MorningSummary> findByTenantIdAndRepIdAndSummaryDate(Long tenantId, Long repId, LocalDate summaryDate);
    
    List<MorningSummary> findByTenantIdAndSummaryDate(Long tenantId, LocalDate summaryDate);
    
    Page<MorningSummary> findByTenantIdAndSummaryDateBetween(Long tenantId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
