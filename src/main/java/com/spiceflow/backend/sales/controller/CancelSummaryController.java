package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.sales.dto.request.CancelSummaryRequest;
import com.spiceflow.backend.sales.dto.response.CancelSummaryResponse;
import com.spiceflow.backend.sales.service.CancelSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/sales/cancel-summaries")
@RequiredArgsConstructor
@Tag(name = "Cancel Summaries", description = "Endpoints for managing cancel summaries")
public class CancelSummaryController {

    private final CancelSummaryService cancelSummaryService;

    @PostMapping
    @Operation(summary = "Create a new cancel summary")
    public ResponseEntity<CancelSummaryResponse> createCancelSummary(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CancelSummaryRequest request) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return new ResponseEntity<>(cancelSummaryService.createCancelSummary(tenantId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing cancel summary")
    public ResponseEntity<CancelSummaryResponse> updateCancelSummary(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody CancelSummaryRequest request) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(cancelSummaryService.updateCancelSummary(tenantId, id, request));
    }

    @GetMapping
    @Operation(summary = "Get all cancel summaries with filtering and pagination")
    public ResponseEntity<Page<CancelSummaryResponse>> getCancelSummaries(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long repId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(cancelSummaryService.getCancelSummaries(tenantId, search, repId, driverId, startDate, endDate, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cancel summary by ID")
    public ResponseEntity<CancelSummaryResponse> getCancelSummaryById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(cancelSummaryService.getCancelSummaryById(tenantId, id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update cancel summary status")
    public ResponseEntity<CancelSummaryResponse> updateCancelSummaryStatus(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @RequestParam String status) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(cancelSummaryService.updateCancelSummaryStatus(tenantId, id, status));
    }

    @PostMapping("/{id}/proceed")
    @Operation(summary = "Proceed with a cancel summary (adds inventory to return warehouse)")
    public ResponseEntity<Void> proceedCancelSummary(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Long> payload) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        Long returnWarehouseId = payload.get("returnWarehouseId");
        if (returnWarehouseId == null) {
            return ResponseEntity.badRequest().build();
        }
        cancelSummaryService.proceedCancelSummary(tenantId, id, returnWarehouseId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/undo-proceed")
    @Operation(summary = "Undo a processed cancel summary (removes inventory from return warehouse)")
    public ResponseEntity<Void> undoProceedCancelSummary(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        cancelSummaryService.undoProceedCancelSummary(tenantId, id);
        return ResponseEntity.ok().build();
    }
}
