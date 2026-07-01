package com.spiceflow.backend.inventory.controller;

import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.inventory.dto.request.WarehouseRequest;
import com.spiceflow.backend.inventory.dto.response.WarehouseResponse;
import com.spiceflow.backend.inventory.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Endpoints for managing inventory warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    @Operation(summary = "List all warehouses (with pagination and search)", description = "Returns warehouses for the authenticated tenant")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF')")
    public ResponseEntity<Page<WarehouseResponse>> getAllWarehouses(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(warehouseService.getAllWarehouses(currentUser.getTenantId(), search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a warehouse by ID")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF')")
    public ResponseEntity<WarehouseResponse> getWarehouse(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(warehouseService.getWarehouse(currentUser.getTenantId(), id));
    }

    @PostMapping
    @Operation(summary = "Create warehouse", description = "Creates a new warehouse for the authenticated tenant")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER')")
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.createWarehouse(currentUser.getTenantId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing warehouse")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER')")
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(currentUser.getTenantId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete warehouse", description = "Soft deletes a warehouse")
    @PreAuthorize("hasRole('TENANT_OWNER')")
    public ResponseEntity<Void> deleteWarehouse(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        warehouseService.deleteWarehouse(currentUser.getTenantId(), id);
        return ResponseEntity.noContent().build();
    }
}
