package com.spiceflow.backend.inventory.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.inventory.dto.request.SupplierRequest;
import com.spiceflow.backend.inventory.dto.response.SupplierResponse;
import com.spiceflow.backend.inventory.service.SupplierService;
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
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Endpoints for managing inventory suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "List all suppliers (with pagination and search)", description = "Returns suppliers for the authenticated tenant", operationId = "getSuppliers")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER', 'PURCHASING_AGENT')")
    public ResponseEntity<Page<SupplierResponse>> getSuppliers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<SupplierResponse> page = supplierService.getSuppliers(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), search, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a supplier by ID", operationId = "getSupplier")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER', 'PURCHASING_AGENT')")
    public ResponseEntity<SupplierResponse> getSupplier(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(supplierService.getSupplier(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), id));
    }

    @PostMapping
    @Operation(summary = "Create supplier", description = "Creates a new supplier for the authenticated tenant", operationId = "createSupplier")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER', 'PURCHASING_AGENT')")
    public ResponseEntity<SupplierResponse> createSupplier(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing supplier", operationId = "updateSupplier")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'PURCHASING_AGENT')")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(supplierService.updateSupplier(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier", description = "Soft deletes a supplier", operationId = "deleteSupplier")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER')")
    public ResponseEntity<Void> deleteSupplier(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        supplierService.deleteSupplier(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), id);
        return ResponseEntity.noContent().build();
    }
}

