package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.sales.dto.request.MorningSummaryRequest;
import com.spiceflow.backend.sales.dto.response.MorningSummaryResponse;
import com.spiceflow.backend.sales.service.MorningSummaryService;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/morning-summaries")
@RequiredArgsConstructor
public class MorningSummaryController {

    private final MorningSummaryService morningSummaryService;

    @PostMapping
    public ResponseEntity<MorningSummaryResponse> createMorningSummary(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody MorningSummaryRequest request) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        MorningSummaryResponse response = morningSummaryService.createMorningSummary(tenantId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<MorningSummaryResponse>> getAllMorningSummaries(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            Pageable pageable) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(morningSummaryService.getAllSummaries(tenantId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MorningSummaryResponse> getMorningSummaryById(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        return ResponseEntity.ok(morningSummaryService.getSummaryById(tenantId, id));
    }
}
