package com.spiceflow.backend.finance.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.finance.dto.request.ExpenseRequest;
import com.spiceflow.backend.finance.dto.response.ExpenseResponse;
import com.spiceflow.backend.finance.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/finance/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "APIs for managing expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Create a new expense", description = "Creates a new expense record", operationId = "createExpense")
    public ResponseEntity<ExpenseResponse> createExpense(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(getTenantId(currentUser), request));
    }

    @GetMapping
    @Operation(summary = "Get expenses for a month", description = "Retrieves all expenses for a given year and month (e.g. 2026-07)", operationId = "getExpensesForMonth")
    public ResponseEntity<List<ExpenseResponse>> getExpensesForMonth(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam("yearMonth") YearMonth yearMonth) {
        return ResponseEntity.ok(expenseService.getExpensesForMonth(getTenantId(currentUser), yearMonth));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense", description = "Deletes an expense by ID", operationId = "deleteExpense")
    public ResponseEntity<Void> deleteExpense(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        expenseService.deleteExpense(getTenantId(currentUser), id);
        return ResponseEntity.noContent().build();
    }

    private Long getTenantId(AuthenticatedUser currentUser) {
        return Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
    }
}
