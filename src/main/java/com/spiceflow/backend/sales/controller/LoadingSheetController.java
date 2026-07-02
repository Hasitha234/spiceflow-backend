package com.spiceflow.backend.sales.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.sales.dto.request.CreateLoadingSheetRequest;
import com.spiceflow.backend.sales.dto.response.LoadingSheetResponse;
import com.spiceflow.backend.sales.service.LoadingSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/sales/loading-sheets")
@RequiredArgsConstructor
@Tag(name = "Loading Sheets", description = "Endpoints for managing loading sheets and lorry loading")
public class LoadingSheetController {

    private final LoadingSheetService loadingSheetService;

    @PostMapping
    @PreAuthorize("hasAuthority('LOADING_WRITE')")
    @Operation(summary = "Create a loading sheet", description = "Generates a loading sheet from a rep order")
    public ResponseEntity<LoadingSheetResponse> createLoadingSheet(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateLoadingSheetRequest request) {
        log.info("User {} creating loading sheet", currentUser.getId());
        LoadingSheetResponse response = loadingSheetService.createLoadingSheet(currentUser.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('LOADING_WRITE')")
    @Operation(summary = "Confirm a loading sheet", description = "Confirms loading and transfers inventory to the vehicle")
    public ResponseEntity<LoadingSheetResponse> confirmLoadingSheet(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("User {} confirming loading sheet {}", currentUser.getId(), id);
        LoadingSheetResponse response = loadingSheetService.confirmLoadingSheet(id, currentUser.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LOADING_READ')")
    @Operation(summary = "List loading sheets", description = "Returns a paginated list of loading sheets")
    public ResponseEntity<Page<LoadingSheetResponse>> getLoadingSheets(
            @AuthenticationPrincipal User currentUser,
            Pageable pageable) {
        log.info("User {} listing loading sheets", currentUser.getId());
        Page<LoadingSheetResponse> response = loadingSheetService.getLoadingSheets(currentUser.getTenantId(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOADING_READ')")
    @Operation(summary = "Get loading sheet by ID", description = "Returns details of a specific loading sheet")
    public ResponseEntity<LoadingSheetResponse> getLoadingSheet(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("User {} getting loading sheet {}", currentUser.getId(), id);
        LoadingSheetResponse response = loadingSheetService.getLoadingSheet(id, currentUser.getTenantId());
        return ResponseEntity.ok(response);
    }
}
