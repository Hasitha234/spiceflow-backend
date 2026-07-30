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

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT b FROM Bill b WHERE b.tenant.id = :tenantId AND " +
           "(:billDate IS NULL OR b.billDate = :billDate) AND " +
           "(:repId IS NULL OR b.rep.id = :repId) AND " +
           "(:shopId IS NULL OR b.shop.id = :shopId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:search IS NULL OR LOWER(b.billNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Bill> findBillsWithFilters(
            @Param("tenantId") Long tenantId,
            @Param("billDate") LocalDate billDate,
            @Param("repId") Long repId,
            @Param("shopId") Long shopId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);

    Optional<Bill> findFirstByTenantIdOrderByCreatedAtDesc(Long tenantId);

    boolean existsByTenantIdAndShopIdAndBillDate(Long tenantId, Long shopId, LocalDate billDate);
}
