package com.spiceflow.backend.finance.service;

import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.finance.dto.request.ExpenseRequest;
import com.spiceflow.backend.finance.dto.response.ExpenseResponse;
import com.spiceflow.backend.finance.entity.Expense;
import com.spiceflow.backend.finance.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse createExpense(Long tenantId, ExpenseRequest request) {
        Expense expense = Expense.builder()
                .tenantId(tenantId)
                .amount(request.amount())
                .category(request.category())
                .description(request.description())
                .date(request.date())
                .build();

        expense = expenseRepository.save(expense);
        return mapToResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesForMonth(Long tenantId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return expenseRepository.findByTenantIdAndDateBetween(tenantId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteExpense(Long tenantId, Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        
        if (!expense.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Expense not found");
        }
        
        expenseRepository.delete(expense);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getDate()
        );
    }
}
