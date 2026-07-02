package com.spiceflow.backend.auth.service;

import com.spiceflow.backend.auth.dto.request.RoleRequest;
import com.spiceflow.backend.auth.dto.response.RoleResponse;
import com.spiceflow.backend.auth.entity.Permission;
import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.PermissionRepository;
import com.spiceflow.backend.auth.repository.RoleRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceConflictException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.spiceflow.backend.common.dto.PageResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<RoleResponse> getRolesForTenant(User currentUser, Pageable pageable) {
        Page<Role> roles = roleRepository.findByTenantIdAndDeletedAtIsNull(currentUser.getTenantId(), pageable);
        
        log.debug("Fetched {} roles from database for tenant {}", roles.getNumberOfElements(), currentUser.getTenantId());
        
        Page<RoleResponse> responsePage = roles.map(this::mapToResponse);
        return PageResponse.of(responsePage);
    }


    @Transactional(rollbackFor = Exception.class)
    public RoleResponse createRole(RoleRequest request, User currentUser) {
        // Check if role name already exists for this tenant
        if (roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(currentUser.getTenantId(), request.getName()).isPresent()) {
            throw new ResourceConflictException("A role with this name already exists");
        }

        // Fetch permissions from database
        Set<Permission> permissions = permissionRepository.findByCodeIn(request.getPermissionCodes());
        if (permissions.isEmpty()) {
            throw new BusinessRuleViolationException("Invalid permission codes provided");
        }

        Role role = Role.builder()
                .tenant(currentUser.getTenant())
                .name(request.getName())
                .description(request.getDescription())
                .isSystemRole(false) // Custom roles are never system roles
                .permissions(permissions)
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("User {} created new role: '{}' (ID: {})", currentUser.getEmail(), savedRole.getName(), savedRole.getId());
        return mapToResponse(savedRole);
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleResponse updateRole(Long roleId, RoleRequest request, User currentUser) {
        Role role = roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(roleId, currentUser.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (role.getIsSystemRole()) {
            throw new BusinessRuleViolationException("System roles (like Owner) cannot be modified");
        }

        // Check if new name conflicts with an existing role
        roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(currentUser.getTenantId(), request.getName())
                .ifPresent(existingRole -> {
                    if (!existingRole.getId().equals(roleId)) {
                        throw new ResourceConflictException("A role with this name already exists");
                    }
                });

        Set<Permission> permissions = permissionRepository.findByCodeIn(request.getPermissionCodes());
        if (permissions.isEmpty()) {
            throw new BusinessRuleViolationException("Invalid permission codes provided");
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(permissions);

        Role savedRole = roleRepository.save(role);
        log.info("User {} updated role: '{}' (ID: {})", currentUser.getEmail(), savedRole.getName(), savedRole.getId());
        return mapToResponse(savedRole);
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId, User currentUser) {
        Role role = roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(roleId, currentUser.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (role.getIsSystemRole()) {
            throw new BusinessRuleViolationException("System roles (like Owner) cannot be deleted");
        }

        if (userRepository.existsByAssignedRoleIdAndDeletedAtIsNull(roleId)) {
            throw new BusinessRuleViolationException("Cannot delete role because it is currently assigned to active users.");
        }

        role.setDeletedAt(OffsetDateTime.now());
        roleRepository.save(role);
        
        log.info("User {} soft-deleted role: '{}' (ID: {})", currentUser.getEmail(), role.getName(), role.getId());
    }


    private RoleResponse mapToResponse(Role role) {
        Set<String> permissionCodes = role.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isSystemRole(role.getIsSystemRole())
                .permissions(permissionCodes)
                .createdAt(role.getCreatedAt())
                .build();
    }
}
