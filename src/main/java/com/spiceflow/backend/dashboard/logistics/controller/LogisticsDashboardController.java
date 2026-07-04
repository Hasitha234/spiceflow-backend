package com.spiceflow.backend.dashboard.logistics.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.dashboard.logistics.dto.LogisticsDashboardResponse;
import com.spiceflow.backend.dashboard.logistics.service.LogisticsDashboardService;
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
@RequestMapping("/api/v1/dashboard/logistics")
@RequiredArgsConstructor
@Tag(name = "Logistics Dashboard", description = "Read-only CQRS projection endpoints for logistics and dispatch operational intelligence")
public class LogisticsDashboardController {

    private final LogisticsDashboardService service;

    @GetMapping
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('LOADING_VIEW') or hasAuthority('DELIVERY_VIEW')")
    @Operation(summary = "Get logistics dashboard projection", description = "Returns active loading sheets, in-progress deliveries, completed counts, and returned item totals")
    public ResponseEntity<LogisticsDashboardResponse> getLogisticsDashboard(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("User {} requesting logistics dashboard projection for tenant {}", user.getId(), user.getTenantId());
        Long tenantId = Objects.requireNonNull(user.getTenantId(), "Tenant ID cannot be null");
        LogisticsDashboardResponse response = service.getDashboard(tenantId, limit);
        return ResponseEntity.ok(response);
    }
}
