package com.spiceflow.backend.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.SupplierRequest;
import com.spiceflow.backend.inventory.dto.response.SupplierResponse;
import com.spiceflow.backend.inventory.entity.Supplier;
import com.spiceflow.backend.inventory.mapper.SupplierMapper;
import com.spiceflow.backend.inventory.repository.SupplierRepository;
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

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock private SupplierRepository supplierRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private SupplierMapper supplierMapper;

    @InjectMocks private SupplierService supplierService;

    private Tenant tenant;
    private Supplier supplier;
    @Mock private SupplierResponse response;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Supplier 1");
        supplier.setTenant(tenant);
    }

    @Test
    void createSupplier_Success() {
        SupplierRequest request = new SupplierRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "Supplier 1");
        
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        SupplierResponse result = supplierService.createSupplier(1L, request);

        assertNotNull(result);
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void getSuppliers_WithSearch() {
        Page<Supplier> page = new PageImpl<>(List.of(supplier));
        when(supplierRepository.findByTenantIdAndNameContainingIgnoreCase(eq(1L), eq("Supp"), any(PageRequest.class))).thenReturn(page);
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        Page<SupplierResponse> result = supplierService.getSuppliers(1L, "Supp", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getSuppliers_WithoutSearch() {
        Page<Supplier> page = new PageImpl<>(List.of(supplier));
        when(supplierRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        Page<SupplierResponse> result = supplierService.getSuppliers(1L, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getSupplier_Success() {
        when(supplierRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        SupplierResponse result = supplierService.getSupplier(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getSupplier_NotFound() {
        when(supplierRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.getSupplier(1L, 1L));
    }

    @Test
    void updateSupplier_Success() {
        SupplierRequest request = new SupplierRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "Updated Supplier");

        when(supplierRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);
        when(supplierMapper.toResponse(supplier)).thenReturn(response);

        SupplierResponse result = supplierService.updateSupplier(1L, 1L, request);

        assertNotNull(result);
        assertEquals("Updated Supplier", supplier.getName());
    }

    @Test
    void deleteSupplier_Success() {
        when(supplierRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(supplier));

        supplierService.deleteSupplier(1L, 1L);

        verify(supplierRepository).delete(supplier);
    }
}
