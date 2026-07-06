package com.spiceflow.backend.auth.service;

import com.spiceflow.backend.auth.dto.request.RoleRequest;
import com.spiceflow.backend.auth.dto.response.RoleResponse;
import com.spiceflow.backend.auth.entity.Permission;
import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    private Long getRequiredTenantId(AuthenticatedUser currentUser) {
        return java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null for role operations");
    }

    @Cacheable(value = "roles")
    public PageResponse<RoleResponse> getRolesForTenant(AuthenticatedUser currentUser, Pageable pageable) {
        Page<Role> roles = roleRepository.findByTenantIdAndDeletedAtIsNull(getRequiredTenantId(currentUser), pageable);
        log.debug("Fetched {} roles from database for tenant {}", roles.getNumberOfElements(), getRequiredTenantId(currentUser));
        Page<RoleResponse> responsePage = roles.map(this::mapToResponse);
        return PageResponse.of(responsePage);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse createRole(RoleRequest request, AuthenticatedUser currentUser) {
        if (roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(getRequiredTenantId(currentUser), request.name()).isPresent()) {
            throw new ResourceConflictException("A role with this name already exists");
        }

        Set<Permission> permissions = permissionRepository.findByCodeIn(request.permissionCodes());
        if (permissions.isEmpty()) {
            throw new BusinessRuleViolationException("Invalid permission codes provided");
        }

        Tenant tenantRef = new Tenant();
        tenantRef.setId(getRequiredTenantId(currentUser));

        Role role = Role.builder()
                .tenant(tenantRef)
                .name(request.name())
                .description(request.description())
                .isSystemRole(false)
                .permissions(permissions)
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("User {} created new role: '{}' (ID: {})", currentUser.getEmail(), savedRole.getName(), savedRole.getId());
        return mapToResponse(savedRole);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "roles", allEntries = true)
    public RoleResponse updateRole(Long roleId, RoleRequest request, AuthenticatedUser currentUser) {
        Role role = roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(roleId, getRequiredTenantId(currentUser))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (role.getIsSystemRole()) {
            throw new BusinessRuleViolationException("System roles (like Owner) cannot be modified");
        }

        roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(getRequiredTenantId(currentUser), request.name())
                .ifPresent(existingRole -> {
                    if (!existingRole.getId().equals(roleId)) {
                        throw new ResourceConflictException("A role with this name already exists");
                    }
                });

        Set<Permission> permissions = permissionRepository.findByCodeIn(request.permissionCodes());
        if (permissions.isEmpty()) {
            throw new BusinessRuleViolationException("Invalid permission codes provided");
        }

        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(permissions);

        Role savedRole = roleRepository.save(role);
        log.info("User {} updated role: '{}' (ID: {})", currentUser.getEmail(), savedRole.getName(), savedRole.getId());
        return mapToResponse(savedRole);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "roles", allEntries = true)
    public void deleteRole(Long roleId, AuthenticatedUser currentUser) {
        Role role = roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(roleId, getRequiredTenantId(currentUser))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (role.getIsSystemRole()) {
            throw new BusinessRuleViolationException("System roles (like Owner) cannot be deleted");
        }

        if (userRepository.existsByAssignedRoleIdAndDeletedAtIsNull(roleId)) {
            throw new BusinessRuleViolationException("Cannot delete role because it is currently assigned to active users.");
        }

        role.setDeletedAt(OffsetDateTime.now(java.time.ZoneId.systemDefault()));
        roleRepository.save(role);
        
        log.info("User {} soft-deleted role: '{}' (ID: {})", currentUser.getEmail(), role.getName(), role.getId());
    }

    private RoleResponse mapToResponse(Role role) {
        Set<String> permissionCodes = role.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getIsSystemRole(),
                permissionCodes,
                role.getCreatedAt()
        );
    }
}

