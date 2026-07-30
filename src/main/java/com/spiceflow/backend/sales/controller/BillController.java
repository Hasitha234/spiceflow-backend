package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.dto.request.BillCollectionRequest;
import com.spiceflow.backend.sales.dto.request.BillRequest;
import com.spiceflow.backend.sales.dto.response.BillResponse;
import com.spiceflow.backend.sales.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@Tag(name = "Bills", description = "Bill management APIs")
public class BillController {

    private final BillService billService;

    @PostMapping
    @Operation(summary = "Create a new bill")
    public ResponseEntity<BillResponse> createBill(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody BillRequest request) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return new ResponseEntity<>(billService.createBill(tenantId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all bills with filtering and pagination")
    public ResponseEntity<Page<BillResponse>> getBills(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) LocalDate billDate,
            @RequestParam(required = false) Long repId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(billService.getBills(tenantId, billDate, repId, shopId, status, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bill by ID")
    public ResponseEntity<BillResponse> getBillById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(billService.getBillById(tenantId, id));
    }

    @PostMapping("/{id}/collect")
    @Operation(summary = "Collect payment for a bill")
    public ResponseEntity<BillResponse> collectBill(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody BillCollectionRequest request) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(billService.collectBill(tenantId, id, request));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a bill")
    public ResponseEntity<BillResponse> cancelBill(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(billService.cancelBill(tenantId, id));
    }
}
