package com.spiceflow.backend.auth.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.request.RoleRequest;
import com.spiceflow.backend.auth.dto.response.RoleResponse;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.service.RoleService;
import com.spiceflow.backend.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/roles")
@Tag(name = "Tenant Roles", description = "Role management for tenant owners")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @Operation(summary = "List all roles for the current tenant")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasRole('OWNER')")
    public ResponseEntity<PageResponse<RoleResponse>> getRoles(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(roleService.getRolesForTenant(currentUser, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new custom role")
    @PreAuthorize("hasAuthority('ROLE_CREATE') or hasRole('OWNER')")
    public RoleResponse createRole(
            @Valid @RequestBody RoleRequest request,
            @AuthenticationPrincipal User currentUser) {
        return roleService.createRole(request, currentUser);
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update a custom role")
    @PreAuthorize("hasAuthority('ROLE_UPDATE') or hasRole('OWNER')")
    public RoleResponse updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleRequest request,
            @AuthenticationPrincipal User currentUser) {
        return roleService.updateRole(roleId, request, currentUser);
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a custom role")
    @PreAuthorize("hasAuthority('ROLE_DELETE') or hasRole('OWNER')")
    public void deleteRole(
            @PathVariable Long roleId,
            @AuthenticationPrincipal User currentUser) {
        roleService.deleteRole(roleId, currentUser);
    }
}
