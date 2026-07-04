package com.spiceflow.backend.dashboard.purchasing.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.dashboard.purchasing.dto.PurchasingDashboardResponse;
import com.spiceflow.backend.dashboard.purchasing.service.PurchasingDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing CQRS Read Model projections for the Purchasing Dashboard.
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/dashboard/purchasing")
@RequiredArgsConstructor
@Tag(name = "Purchasing Dashboard", description = "Endpoints for purchasing analytics and KPI projections")
public class PurchasingDashboardController {

    private final PurchasingDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('PURCHASE_VIEW')")
    @Operation(summary = "Get purchasing dashboard projection", description = "Returns open order metrics, aging buckets, supplier lead times, and recent orders", operationId = "getPurchasingDashboard")
    public ResponseEntity<PurchasingDashboardResponse> getPurchasingDashboard(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        log.info("User {} fetching purchasing dashboard projection", currentUser.getId());
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        PurchasingDashboardResponse response = dashboardService.getDashboard(tenantId, limit);
        return ResponseEntity.ok(response);
    }
}
