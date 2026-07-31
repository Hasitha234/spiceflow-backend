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

import org.springframework.lang.Nullable;

@Repository
public interface FinalBalanceRepository extends JpaRepository<FinalBalance, Long> {

    @Query("SELECT f FROM FinalBalance f " +
           "WHERE f.tenant.id = :tenantId " +
           "AND (cast(:repId as long) IS NULL OR f.rep.id = :repId) " +
           "AND (cast(:balanceDate as date) IS NULL OR f.balanceDate = :balanceDate)")
    Page<FinalBalance> findByTenantAndFilters(
            @Param("tenantId") Long tenantId,
            @Nullable @Param("repId") Long repId,
            @Nullable @Param("balanceDate") LocalDate balanceDate,
            Pageable pageable);

    boolean existsByTenantIdAndRepIdAndBalanceDate(Long tenantId, Long repId, LocalDate balanceDate);
}
