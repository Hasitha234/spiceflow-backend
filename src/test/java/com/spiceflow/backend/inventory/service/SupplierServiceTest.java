package com.spiceflow.backend.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spiceflow.backend.inventory.dto.response.SupplierResponse;
import com.spiceflow.backend.inventory.entity.Supplier;
import com.spiceflow.backend.inventory.repository.SupplierRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private com.spiceflow.backend.inventory.mapper.SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier mockSupplier;

    @BeforeEach
    void setUp() {
        mockSupplier = new Supplier();
        mockSupplier.setId(10L);
        mockSupplier.setName("Supplier A");
    }

    @Test
    void testGetSuppliers() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Supplier> page = new PageImpl<>(List.of(mockSupplier));

        when(supplierRepository.findByTenantId(eq(1L), any(Pageable.class))).thenReturn(page);

        SupplierResponse mockResponse = SupplierResponse.builder().id(10L).name("Supplier A").build();
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(mockResponse);

        Page<SupplierResponse> response = supplierService.getSuppliers(1L, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Supplier A", response.getContent().get(0).getName());
    }

    @Test
    void testGetSuppliers_WithSearch() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Supplier> page = new PageImpl<>(List.of(mockSupplier));

        when(supplierRepository.findByTenantIdAndNameContainingIgnoreCase(eq(1L), eq("Search"), any(Pageable.class))).thenReturn(page);

        SupplierResponse mockResponse = SupplierResponse.builder().id(10L).name("Search Supplier").build();
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(mockResponse);

        Page<SupplierResponse> response = supplierService.getSuppliers(1L, "Search", pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Search Supplier", response.getContent().get(0).getName());
    }
}
