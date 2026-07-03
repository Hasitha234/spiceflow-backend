package com.spiceflow.backend.sales.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.dto.response.RepPerformanceResponse;
import com.spiceflow.backend.sales.dto.response.SalesSummaryResponse;
import com.spiceflow.backend.sales.dto.response.ShopOutstandingResponse;
import com.spiceflow.backend.sales.dto.response.StockStatusResponse;
import com.spiceflow.backend.sales.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for generating various reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales-summary")
    @PreAuthorize("hasAuthority('REPORTS_VIEW')")
    @Operation(summary = "Get sales summary", description = "Returns a summary of sales, returns, and collections for a date range")
    public CompletableFuture<ResponseEntity<SalesSummaryResponse>> getSalesSummary(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("User {} requesting sales summary", currentUser.getId());
        return reportService.getSalesSummary(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), startDate, endDate)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/shop-outstanding")
    @PreAuthorize("hasAuthority('REPORTS_VIEW')")
    @Operation(summary = "Get shop outstanding", description = "Returns a list of shops with their outstanding balances")
    public ResponseEntity<List<ShopOutstandingResponse>> getShopOutstanding(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        log.info("User {} requesting shop outstanding report", currentUser.getId());
        return ResponseEntity.ok(reportService.getShopOutstandings(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null")));
    }

    @GetMapping("/stock-status")
    @PreAuthorize("hasAuthority('REPORTS_VIEW')")
    @Operation(summary = "Get stock status", description = "Returns the current status of product stocks across warehouses")
    public ResponseEntity<List<StockStatusResponse>> getStockStatus(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        log.info("User {} requesting stock status report", currentUser.getId());
        return ResponseEntity.ok(reportService.getStockStatus(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null")));
    }
    
    @GetMapping("/rep-performance")
    @PreAuthorize("hasAuthority('REPORTS_VIEW')")
    @Operation(summary = "Get rep performance", description = "Returns performance metrics for sales reps")
    public ResponseEntity<List<RepPerformanceResponse>> getRepPerformance(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("User {} requesting rep performance report", currentUser.getId());
        return ResponseEntity.ok(reportService.getRepPerformance(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), startDate, endDate));
    }
}


