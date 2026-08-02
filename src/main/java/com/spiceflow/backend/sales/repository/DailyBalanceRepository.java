package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.DailyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface DailyBalanceRepository extends JpaRepository<DailyBalance, Long> {

    Optional<DailyBalance> findByTenantIdAndBalanceDate(Long tenantId, LocalDate balanceDate);
    
    boolean existsByTenantIdAndBalanceDate(Long tenantId, LocalDate balanceDate);

    List<DailyBalance> findByTenantIdAndBalanceDateBetween(Long tenantId, LocalDate startDate, LocalDate endDate);
}
