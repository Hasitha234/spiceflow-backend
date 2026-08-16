package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.common.context.TenantContext;
import com.spiceflow.backend.sales.dto.request.EveningSummaryRequest;
import com.spiceflow.backend.sales.dto.response.EveningSummaryResponse;
import com.spiceflow.backend.sales.dto.response.StockAvailabilityResponse;
import com.spiceflow.backend.sales.service.EveningSummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sales/evening-summaries")
@RequiredArgsConstructor
public class EveningSummaryController {

    private final EveningSummaryService eveningSummaryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'DATA_ENTRY')")
    public ResponseEntity<EveningSummaryResponse> createEveningSummary(@Valid @RequestBody EveningSummaryRequest request) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(eveningSummaryService.createEveningSummary(tenantId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'DATA_ENTRY')")
    public ResponseEntity<EveningSummaryResponse> updateEveningSummary(
            @PathVariable Long id,
            @Valid @RequestBody EveningSummaryRequest request) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(eveningSummaryService.updateEveningSummary(tenantId, id, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'DATA_ENTRY', 'VIEW_ONLY')")
    public ResponseEntity<Page<EveningSummaryResponse>> getEveningSummaries(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long repId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(eveningSummaryService.getEveningSummaries(tenantId, search, repId, driverId, startDate, endDate, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'DATA_ENTRY', 'VIEW_ONLY')")
    public ResponseEntity<EveningSummaryResponse> getEveningSummaryById(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(eveningSummaryService.getEveningSummaryById(tenantId, id));
    }

    @GetMapping("/{id}/stock-check")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'DATA_ENTRY')")
    public ResponseEntity<List<StockAvailabilityResponse>> checkStockAvailability(
            @PathVariable Long id,
            @RequestParam Long warehouseId) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(eveningSummaryService.checkStockAvailability(tenantId, id, warehouseId));
    }

    @PostMapping("/{id}/proceed")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'DATA_ENTRY')")
    public ResponseEntity<Void> proceedEveningSummary(
            @PathVariable Long id,
            @RequestBody Map<String, Long> payload) {
        Long tenantId = TenantContext.getTenantId();
        Long warehouseId = payload.get("warehouseId");
        if (warehouseId == null) {
            throw new IllegalArgumentException("Warehouse ID is required to proceed.");
        }
        eveningSummaryService.proceedEveningSummary(tenantId, id, warehouseId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/undo-proceed")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'DATA_ENTRY')")
    public ResponseEntity<Void> undoProceedEveningSummary(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        eveningSummaryService.undoProceedEveningSummary(tenantId, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'DATA_ENTRY')")
    public ResponseEntity<Void> deleteEveningSummary(@PathVariable Long id) {
        Long tenantId = TenantContext.getTenantId();
        eveningSummaryService.deleteEveningSummary(tenantId, id);
        return ResponseEntity.noContent().build();
    }
}
