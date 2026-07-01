package com.spiceflow.backend.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.dto.request.RoleRequest;
import com.spiceflow.backend.auth.dto.response.RoleResponse;
import com.spiceflow.backend.auth.entity.Permission;
import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.PermissionRepository;
import com.spiceflow.backend.auth.repository.RoleRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;
    @Mock private PermissionRepository permissionRepository;

    @InjectMocks private RoleService roleService;

    private User testUser;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testTenant = Tenant.builder().build();
        testTenant.setId(1L);
        testUser = User.builder().tenant(testTenant).build();
        testUser.setId(100L);
    }

    @Test
    void createRole_Success() {
        // 1. Arrange (Setup the test data)
        RoleRequest request = new RoleRequest();
        request.setName("Manager");
        request.setPermissionCodes(Set.of("PURCHASE_VIEW"));

        Permission perm = Permission.builder().code("PURCHASE_VIEW").build();

        // When the service asks the repo if the name exists, say No (empty)
        when(roleRepository.findByTenantIdAndNameAndDeletedAtIsNull(1L, "Manager"))
            .thenReturn(Optional.empty());
        
        // When it fetches the permissions, return our mock permission
        when(permissionRepository.findByCodeIn(Set.of("PURCHASE_VIEW")))
            .thenReturn(Set.of(perm));

        // When it saves the role, return the saved role with an ID
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        // 2. Act (Call the method)
        RoleResponse response = roleService.createRole(request, testUser);

        // 3. Assert (Verify the results)
        assertNotNull(response);
        assertEquals("Manager", response.getName());
        assertFalse(response.getIsSystemRole());
        assertTrue(response.getPermissions().contains("PURCHASE_VIEW"));
    }

    @Test
    void deleteRole_Fails_IfSystemRole() {
        // 1. Arrange
        Role systemRole = Role.builder()
            .isSystemRole(true) // Crucial!
            .build();
        systemRole.setId(10L);

        when(roleRepository.findByIdAndTenantIdAndDeletedAtIsNull(10L, 1L))
            .thenReturn(Optional.of(systemRole));

        // 2. Act & Assert
        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class, 
            () -> roleService.deleteRole(10L, testUser)
        );

        assertEquals("System roles (like Owner) cannot be deleted", exception.getMessage());
        
        // Verify delete/save was NEVER called
        verify(roleRepository, never()).save(any(Role.class));
    }
}
