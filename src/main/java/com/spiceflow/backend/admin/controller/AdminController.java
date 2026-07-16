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
import com.spiceflow.backend.admin.dto.request.UpdateTenantRequest;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.spiceflow.backend.common.dto.PageResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;


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

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Update Tenant Status", description = "Toggle tenant between ACTIVE and SUSPENDED")
  public ResponseEntity<TenantResponse> updateTenantStatus(
      @PathVariable Long id,
      @Valid @RequestBody com.spiceflow.backend.admin.dto.request.UpdateTenantStatusRequest request) {
    return ResponseEntity.ok(adminService.updateTenantStatus(id, request));
  }

  // --- USER MANAGEMENT ENDPOINTS ---

  @PostMapping("/users")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Create User", description = "Create a new user (Business Owner, Data Entry, or Driver)")
  public ResponseEntity<com.spiceflow.backend.admin.dto.response.UserResponse> createUser(
      @Valid @RequestBody com.spiceflow.backend.admin.dto.request.CreateUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "List Users", description = "List all users across all tenants")
  public ResponseEntity<PageResponse<com.spiceflow.backend.admin.dto.response.UserResponse>> getAllUsers(
      @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
    return ResponseEntity.ok(adminService.getAllUsers(pageable));
  }

  @GetMapping("/users/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get User by ID")
  public ResponseEntity<com.spiceflow.backend.admin.dto.response.UserResponse> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(adminService.getUserById(id));
  }

  @PutMapping("/users/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Update User")
  public ResponseEntity<com.spiceflow.backend.admin.dto.response.UserResponse> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody com.spiceflow.backend.admin.dto.request.UpdateUserRequest request) {
    return ResponseEntity.ok(adminService.updateUser(id, request));
  }

  @DeleteMapping("/users/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Soft-Delete User")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    adminService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/users/{userId}/tenants")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Assign Tenant to Business Owner")
  public ResponseEntity<Void> assignTenant(
      @PathVariable Long userId,
      @RequestBody java.util.Map<String, Long> payload) {
    Long tenantId = java.util.Objects.requireNonNull(payload.get("tenantId"), "tenantId is required");
    adminService.assignTenantToOwner(userId, tenantId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/users/{userId}/tenants/{tenantId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Remove Tenant from Business Owner")
  public ResponseEntity<Void> removeTenant(
      @PathVariable Long userId,
      @PathVariable Long tenantId) {
    adminService.removeTenantFromOwner(userId, tenantId);
    return ResponseEntity.ok().build();
  }
}


