package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.FinalBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FinalBalanceRepository extends JpaRepository<FinalBalance, Long> {

    @Query("SELECT f FROM FinalBalance f " +
           "WHERE f.tenant.id = :tenantId " +
           "AND (:repId IS NULL OR f.rep.id = :repId) " +
           "AND (CAST(:balanceDate AS date) IS NULL OR f.balanceDate = :balanceDate)")
    Page<FinalBalance> findByTenantAndFilters(
            @Param("tenantId") Long tenantId,
            @Param("repId") Long repId,
            @Param("balanceDate") LocalDate balanceDate,
            Pageable pageable);

    boolean existsByTenantIdAndRepIdAndBalanceDate(Long tenantId, Long repId, LocalDate balanceDate);
}
