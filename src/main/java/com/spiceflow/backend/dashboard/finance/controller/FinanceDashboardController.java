package com.spiceflow.backend.dashboard.finance.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.dashboard.finance.dto.FinanceDashboardResponse;
import com.spiceflow.backend.dashboard.finance.service.FinanceDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard/finance")
@RequiredArgsConstructor
@Tag(name = "Finance Dashboard", description = "Read-only CQRS projection endpoints for receivables, payables, cash flow, and financial ledger intelligence")
public class FinanceDashboardController {

    private final FinanceDashboardService service;

    @GetMapping
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('PAYMENT_VIEW') or hasAuthority('REPORT_DAILY') or hasAuthority('REPORT_MONTHLY')")
    @Operation(summary = "Get finance dashboard projection", description = "Returns total receivables/payables, net cash flow, collections, aging breakdown, and recent transactions")
    public ResponseEntity<FinanceDashboardResponse> getFinanceDashboard(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("User {} requesting finance dashboard projection for tenant {}", user.getId(), user.getTenantId());
        Long tenantId = Objects.requireNonNull(user.getTenantId(), "Tenant ID cannot be null");
        FinanceDashboardResponse response = service.getDashboard(tenantId, limit);
        return ResponseEntity.ok(response);
    }
}
