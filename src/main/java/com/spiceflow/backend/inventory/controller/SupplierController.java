package com.spiceflow.backend.inventory.controller;

import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.inventory.dto.request.SupplierRequest;
import com.spiceflow.backend.inventory.dto.response.SupplierResponse;
import com.spiceflow.backend.inventory.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Endpoints for managing inventory suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "List all suppliers", description = "Returns all suppliers for the authenticated tenant")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER', 'PURCHASING_AGENT')")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(supplierService.getAllSuppliers(currentUser.getTenantId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a supplier by ID")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'INVENTORY_MANAGER', 'PURCHASING_AGENT')")
    public ResponseEntity<SupplierResponse> getSupplier(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(supplierService.getSupplier(currentUser.getTenantId(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new supplier")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'PURCHASING_AGENT')")
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierRequest request,
            @AuthenticationPrincipal User currentUser) {
        SupplierResponse response = supplierService.createSupplier(currentUser.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing supplier")
    @PreAuthorize("hasAnyRole('TENANT_OWNER', 'PURCHASING_AGENT')")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(supplierService.updateSupplier(currentUser.getTenantId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier (Soft Delete)")
    @PreAuthorize("hasRole('TENANT_OWNER')")
    public ResponseEntity<Void> deleteSupplier(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        supplierService.deleteSupplier(currentUser.getTenantId(), id);
        return ResponseEntity.noContent().build();
    }
}
