package com.spiceflow.backend.finance.repository;

import com.spiceflow.backend.finance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByTenantIdAndDateBetween(Long tenantId, LocalDate startDate, LocalDate endDate);
}
