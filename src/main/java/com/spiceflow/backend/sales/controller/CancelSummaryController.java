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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @Parameter(hidden = true) @RequestAttribute("tenantId") Long tenantId,
            @Valid @RequestBody CancelSummaryRequest request) {
        return new ResponseEntity<>(cancelSummaryService.createCancelSummary(tenantId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all cancel summaries with filtering and pagination")
    public ResponseEntity<Page<CancelSummaryResponse>> getCancelSummaries(
            @Parameter(hidden = true) @RequestAttribute("tenantId") Long tenantId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long repId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(cancelSummaryService.getCancelSummaries(tenantId, search, repId, driverId, startDate, endDate, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cancel summary by ID")
    public ResponseEntity<CancelSummaryResponse> getCancelSummaryById(
            @Parameter(hidden = true) @RequestAttribute("tenantId") Long tenantId,
            @PathVariable Long id) {
        return ResponseEntity.ok(cancelSummaryService.getCancelSummaryById(tenantId, id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update cancel summary status")
    public ResponseEntity<CancelSummaryResponse> updateCancelSummaryStatus(
            @Parameter(hidden = true) @RequestAttribute("tenantId") Long tenantId,
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(cancelSummaryService.updateCancelSummaryStatus(tenantId, id, status));
    }
}
