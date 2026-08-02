package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.dto.response.DailyBalanceResponse;
import com.spiceflow.backend.sales.service.DailyBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/daily-balance")
@RequiredArgsConstructor
@Tag(name = "Daily Balance", description = "Endpoints for reconciling and proceeding daily balances")
public class DailyBalanceController {

    private final DailyBalanceService dailyBalanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_TENANT_OWNER') or hasAuthority('ROLE_DATA_ENTRY')")
    @Operation(summary = "Get daily balance for a date", operationId = "getDailyBalance")
    public ResponseEntity<DailyBalanceResponse> getDailyBalance(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("User {} requesting daily balance for date {}", currentUser.getId(), date);
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(dailyBalanceService.getDailyBalance(tenantId, date));
    }

    @PostMapping("/proceed")
    @PreAuthorize("hasAuthority('ROLE_TENANT_OWNER') or hasAuthority('ROLE_DATA_ENTRY')")
    @Operation(summary = "Proceed daily balance for a date", operationId = "proceedDailyBalance")
    public ResponseEntity<DailyBalanceResponse> proceedDailyBalance(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("User {} proceeding daily balance for date {}", currentUser.getId(), date);
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(dailyBalanceService.proceedDailyBalance(tenantId, date));
    }

    @PostMapping("/undo")
    @PreAuthorize("hasAuthority('ROLE_TENANT_OWNER')")
    @Operation(summary = "Undo daily balance for a date", operationId = "undoDailyBalance")
    public ResponseEntity<DailyBalanceResponse> undoDailyBalance(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("User {} undoing daily balance for date {}", currentUser.getId(), date);
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(dailyBalanceService.undoDailyBalance(tenantId, date));
    }
}
