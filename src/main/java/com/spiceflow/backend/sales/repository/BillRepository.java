package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

import java.util.List;

import org.springframework.lang.Nullable;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT b FROM Bill b WHERE b.tenant.id = :tenantId AND " +
           "(cast(:startDate as date) IS NULL OR b.billDate >= :startDate) AND " +
           "(cast(:endDate as date) IS NULL OR b.billDate <= :endDate) AND " +
           "(cast(:repId as long) IS NULL OR b.rep.id = :repId) AND " +
           "(cast(:shopId as long) IS NULL OR b.shop.id = :shopId) AND " +
           "(cast(:status as text) IS NULL OR b.status = :status) AND " +
           "(cast(:search as text) IS NULL OR LOWER(b.billNumber) LIKE LOWER(CONCAT('%', cast(:search as text), '%'))) " +
           "ORDER BY b.billDate DESC")
    Page<Bill> findBillsWithFilters(
            @Param("tenantId") Long tenantId,
            @Nullable @Param("startDate") LocalDate startDate,
            @Nullable @Param("endDate") LocalDate endDate,
            @Nullable @Param("repId") Long repId,
            @Nullable @Param("shopId") Long shopId,
            @Nullable @Param("status") String status,
            @Nullable @Param("search") String search,
            Pageable pageable);

    Optional<Bill> findFirstByTenantIdOrderByCreatedAtDesc(Long tenantId);

    boolean existsByTenantIdAndShopIdAndBillDate(Long tenantId, Long shopId, LocalDate billDate);

    List<Bill> findByTenantIdAndRepIdAndBillDate(Long tenantId, Long repId, LocalDate billDate);
    
    List<Bill> findByTenantIdAndBillDate(Long tenantId, LocalDate billDate);
}
