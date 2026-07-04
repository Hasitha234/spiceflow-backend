package com.spiceflow.backend.dashboard.inventory.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.dashboard.inventory.dto.InventoryDashboardResponse;
import com.spiceflow.backend.dashboard.inventory.service.InventoryDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/dashboard/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Dashboard", description = "Read-only CQRS projection endpoints for inventory operational intelligence")
public class InventoryDashboardController {

    private final InventoryDashboardService service;

    @GetMapping
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "Get inventory dashboard projection", description = "Returns stock valuations, low stock counts/items, pending transfers, and recent ledger movements")
    public ResponseEntity<InventoryDashboardResponse> getInventoryDashboard(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("User {} requesting inventory dashboard projection for tenant {}", user.getId(), user.getTenantId());
        Long tenantId = java.util.Objects.requireNonNull(user.getTenantId(), "Tenant ID cannot be null");
        InventoryDashboardResponse response = service.getDashboard(tenantId, limit);
        return ResponseEntity.ok(response);
    }
}
