package com.spiceflow.backend.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.admin.dto.request.CreateTenantRequest;
import com.spiceflow.backend.admin.dto.request.UpdateTenantRequest;
import com.spiceflow.backend.admin.dto.response.TenantResponse;
import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
import com.spiceflow.backend.auth.entity.Permission;
import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.PermissionRepository;
import com.spiceflow.backend.auth.repository.RoleRepository;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import com.spiceflow.backend.common.dto.PageResponse;
import com.spiceflow.backend.common.exception.ResourceConflictException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PermissionRepository permissionRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private BusinessTypeRepository businessTypeRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private com.spiceflow.backend.auth.repository.BusinessOwnerTenantRepository businessOwnerTenantRepository;

    @InjectMocks private AdminService adminService;

    private Tenant tenant;
    private BusinessType businessType;

    @BeforeEach
    void setUp() {
        businessType = new BusinessType();
        businessType.setId(1L);
        businessType.setName("Retail");

        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setBusinessName("Test Business");
        tenant.setBusinessType(businessType);
        tenant.setEmail("owner@test.com");
        tenant.setStatus("ACTIVE");
        tenant.setPlan("BASIC");
    }

    @Test
    void createTenant_Success() {
        CreateTenantRequest request = CreateTenantRequest.builder().businessName("Test Biz").businessTypeId(1L).ownerEmail("owner@test.com").ownerPassword("pass1234").build();
                                
        when(tenantRepository.findByEmailAndDeletedAtIsNull(request.ownerEmail())).thenReturn(Optional.empty());
        when(businessTypeRepository.findById(request.businessTypeId())).thenReturn(Optional.of(businessType));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        
        Role mockRole = new Role();
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);
        when(passwordEncoder.encode(request.ownerPassword())).thenReturn("hashed_pwd");

        TenantResponse response = adminService.createTenant(request);

        assertNotNull(response);
        assertEquals(tenant.getBusinessName(), response.businessName());
        verify(tenantRepository).save(any(Tenant.class));
        verify(userRepository).save(any(User.class));
        verify(warehouseRepository).saveAll(anyList());
    }

    @Test
    void createTenant_EmailAlreadyExists() {
        CreateTenantRequest request = CreateTenantRequest.builder().businessName("Test Biz").businessTypeId(1L).ownerEmail("owner@test.com").ownerPassword("pass1234").build();
        
        when(tenantRepository.findByEmailAndDeletedAtIsNull(request.ownerEmail())).thenReturn(Optional.of(tenant));

        assertThrows(ResourceConflictException.class, () -> adminService.createTenant(request));
    }

    @Test
    void getAllTenants_ReturnsPageResponse() {
        Page<Tenant> page = new PageImpl<>(List.of(tenant));
        when(tenantRepository.findAllByDeletedAtIsNull(any(PageRequest.class))).thenReturn(page);

        PageResponse<TenantResponse> response = adminService.getAllTenants(PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(tenant.getBusinessName(), response.getContent().get(0).businessName());
    }

    @Test
    void getTenantById_Success() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        TenantResponse response = adminService.getTenantById(1L);

        assertNotNull(response);
        assertEquals(tenant.getBusinessName(), response.businessName());
    }

    @Test
    void getTenantById_NotFound() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getTenantById(1L));
    }

    @Test
    void updateTenant_Success() {
        UpdateTenantRequest request = UpdateTenantRequest.builder().businessName("Updated Business").businessTypeId(1L).status("ACTIVE").plan("PRO").build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(businessTypeRepository.findById(1L)).thenReturn(Optional.of(businessType));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        TenantResponse response = adminService.updateTenant(1L, request);

        assertNotNull(response);
        verify(tenantRepository).save(tenant);
        assertEquals("Updated Business", tenant.getBusinessName());
    }

    @Test
    void deleteTenant_Success() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        adminService.deleteTenant(1L);

        assertNotNull(tenant.getDeletedAt());
        verify(tenantRepository).save(tenant);
    }
}
