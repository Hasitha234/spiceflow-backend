package com.spiceflow.backend.admin.controller;

import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.spiceflow.backend.admin.dto.request.CreateTenantRequest;
import com.spiceflow.backend.admin.dto.response.TenantResponse;
import com.spiceflow.backend.admin.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestController;
import com.spiceflow.backend.admin.dto.request.UpdateTenantRequest;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.spiceflow.backend.common.dto.PageResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@Validated
@RequestMapping("/api/v1/admin/tenants")
@Tag(name = "1. Super Admin Operations", description = "Endpoints restricted to the Platform Admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Creates a new business (Tenant) and its owner account.
     * Only accessible by Platform Admins.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Provision a new Tenant", description = "Creates a new business and its owner account. Returns 201 Created on success.", operationId = "createTenant")
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = adminService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

  @GetMapping
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "List all Tenants", description = "Returns all active businesses on the platform.", operationId = "getAllTenants")
  public ResponseEntity<PageResponse<TenantResponse>> getAllTenants(
      @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
    return ResponseEntity.ok(adminService.getAllTenants(pageable));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get Tenant by ID", description = "Returns a single business by its ID. Returns 404 if not found or deleted.", operationId = "getTenantById")
  public ResponseEntity<TenantResponse> getTenantById(@PathVariable Long id) {
    return ResponseEntity.ok(adminService.getTenantById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Update Tenant", description = "Updates an existing business's name, type, status, and plan.", operationId = "updateTenant")
  public ResponseEntity<TenantResponse> updateTenant(
      @PathVariable Long id,
      @Valid @RequestBody UpdateTenantRequest request) {
    return ResponseEntity.ok(adminService.updateTenant(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @ApiResponse(responseCode = "204", description = "Tenant successfully deleted")
  @Operation(summary = "Soft-Delete Tenant", description = "Marks the business as deleted. Data is preserved for audit history. Returns 204 on success.", operationId = "deleteTenant")
  public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
    adminService.deleteTenant(id);
    return ResponseEntity.noContent().build();
  }

}


