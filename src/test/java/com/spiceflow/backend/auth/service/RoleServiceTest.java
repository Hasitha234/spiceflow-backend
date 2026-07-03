package com.spiceflow.backend.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.spiceflow.backend.common.dto.PageResponse;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceConflictException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;
    @Mock private PermissionRepository permissionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private RoleService roleService;

    private User currentUser;
    private Tenant tenant;
    private Role role;
    private Permission permission;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setTenant(tenant);

        permission = new Permission();
        permission.setId(1L);
        permission.setCode("MANAGE_USERS");

        role = new Role();
        role.setId(1L);
        role.setTenant(tenant);
        role.setName("Manager");
        role.setIsSystemRole(false);
        role.setPermissions(Set.of(permission));
    }

    @Test
    void getRolesForTenant_Success() {
        Page<Role> page = new PageImpl<>(List.of(role));
        when(roleRepository.findByTenantIdAndDeletedAtIsNull(eq(1L), any(PageRequest.class))).thenReturn(page);

        PageResponse<RoleResponse> response = roleService.getRolesForTenant(com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build(), PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Manager", response.getContent().get(0).name());
    }

    @Test
    void createRole_Success() {
        RoleRequest request = RoleRequest.builder().name("New Role").description("Admin role").permissionCodes(java.util.Set.of("MANAGE_USERS")).build();

        when(roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(1L, "New Role")).thenReturn(Optional.empty());
        when(permissionRepository.findByCodeIn(anySet())).thenReturn(Set.of(permission));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleResponse response = roleService.createRole(request, com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build());

        assertNotNull(response);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void createRole_RoleExists() {
        RoleRequest request = RoleRequest.builder().name("Manager").description("Manager role").permissionCodes(java.util.Set.of("PERM_READ")).build();

        when(roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(1L, "Manager")).thenReturn(Optional.of(role));

        assertThrows(ResourceConflictException.class, () -> roleService.createRole(request, com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build()));
    }

    @Test
    void createRole_InvalidPermissions() {
        RoleRequest request = RoleRequest.builder().name("New Role").description("Role").permissionCodes(java.util.Set.of("INVALID_CODE")).build();

        when(roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(1L, "New Role")).thenReturn(Optional.empty());
        when(permissionRepository.findByCodeIn(anySet())).thenReturn(Set.of());

        assertThrows(BusinessRuleViolationException.class, () -> roleService.createRole(request, com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build()));
    }

    @Test
    void updateRole_Success() {
        RoleRequest request = RoleRequest.builder().name("Updated Role").description("Admin role").permissionCodes(java.util.Set.of("MANAGE_USERS")).build();

        when(roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(role));
        when(roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(1L, "Updated Role")).thenReturn(Optional.empty());
        when(permissionRepository.findByCodeIn(anySet())).thenReturn(Set.of(permission));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleResponse response = roleService.updateRole(1L, request, com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build());

        assertNotNull(response);
    }

    @Test
    void updateRole_SystemRole() {
        role.setIsSystemRole(true);
        when(roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(role));

        assertThrows(BusinessRuleViolationException.class, () -> roleService.updateRole(1L, RoleRequest.builder().name("Admin").description("Admin role").permissionCodes(java.util.Set.of("PERM_READ")).build(), com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build()));
    }

    @Test
    void updateRole_RoleNameConflict() {
        RoleRequest request = RoleRequest.builder().name("Existing Role").description("Admin role").permissionCodes(java.util.Set.of("PERM_READ")).build();

        Role existingRole = new Role();
        existingRole.setId(2L);

        when(roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(role));
        when(roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(1L, "Existing Role")).thenReturn(Optional.of(existingRole));

        assertThrows(ResourceConflictException.class, () -> roleService.updateRole(1L, request, com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build()));
    }

    @Test
    void deleteRole_Success() {
        when(roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(role));
        when(userRepository.existsByAssignedRoleIdAndDeletedAtIsNull(1L)).thenReturn(false);

        roleService.deleteRole(1L, com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build());

        verify(roleRepository).save(role);
        assertNotNull(role.getDeletedAt());
    }

    @Test
    void deleteRole_SystemRole() {
        role.setIsSystemRole(true);
        when(roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(role));

        assertThrows(BusinessRuleViolationException.class, () -> roleService.deleteRole(1L, com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build()));
    }

    @Test
    void deleteRole_RoleInUse() {
        when(roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(role));
        when(userRepository.existsByAssignedRoleIdAndDeletedAtIsNull(1L)).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> roleService.deleteRole(1L, com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(currentUser.getId()).tenantId(currentUser.getTenant() != null ? currentUser.getTenant().getId() : null).email(currentUser.getEmail()).build()));
    }
}





