package com.spiceflow.backend.dashboard.sales.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.dashboard.sales.dto.SalesDashboardResponse;
import com.spiceflow.backend.dashboard.sales.service.SalesDashboardService;
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
@RequestMapping("/api/v1/dashboard/sales")
@RequiredArgsConstructor
@Tag(name = "Sales Dashboard", description = "Read-only CQRS projection endpoints for sales, orders, and shop receivables operational intelligence")
public class SalesDashboardController {

    private final SalesDashboardService service;

    @GetMapping
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('ORDER_VIEW') or hasAuthority('DELIVERY_VIEW') or hasAuthority('PAYMENT_VIEW')")
    @Operation(summary = "Get sales dashboard projection", description = "Returns today/month sales, collections, outstanding shop loans, recent rep orders, and top debtors")
    public ResponseEntity<SalesDashboardResponse> getSalesDashboard(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("User {} requesting sales dashboard projection for tenant {}", user.getId(), user.getTenantId());
        Long tenantId = Objects.requireNonNull(user.getTenantId(), "Tenant ID cannot be null");
        SalesDashboardResponse response = service.getDashboard(tenantId, limit);
        return ResponseEntity.ok(response);
    }
}
