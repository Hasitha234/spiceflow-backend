package com.spiceflow.backend.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.inventory.dto.request.WarehouseRequest;
import com.spiceflow.backend.inventory.dto.response.WarehouseResponse;
import com.spiceflow.backend.inventory.entity.Warehouse;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private com.spiceflow.backend.inventory.mapper.WarehouseMapper warehouseMapper;

    @InjectMocks
    private WarehouseService warehouseService;

    private Tenant mockTenant;
    private Warehouse mockWarehouse;

    @BeforeEach
    void setUp() {
        mockTenant = new Tenant();
        mockTenant.setId(1L);

        mockWarehouse = Warehouse.builder()
                .name("Main Hub")
                .location("New York")
                .tenant(mockTenant)
                .build();
        mockWarehouse.setId(10L);
    }

    @Test
    void testGetAllWarehouses_WithoutSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Warehouse> page = new PageImpl<>(List.of(mockWarehouse));

        when(warehouseRepository.findByTenantId(1L, pageable)).thenReturn(page);

        WarehouseResponse mockResponse = WarehouseResponse.builder().id(10L).name("Main Hub").build();
        when(warehouseMapper.toResponse(any(Warehouse.class))).thenReturn(mockResponse);

        Page<WarehouseResponse> result = warehouseService.getAllWarehouses(1L, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Main Hub", result.getContent().get(0).name());
        verify(warehouseRepository).findByTenantId(1L, pageable);
    }

    @Test
    void testGetAllWarehouses_WithSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Warehouse> page = new PageImpl<>(List.of(mockWarehouse));

        when(warehouseRepository.findByTenantIdAndNameContainingIgnoreCase(1L, "Hub", pageable)).thenReturn(page);

        WarehouseResponse mockResponse = WarehouseResponse.builder().id(10L).name("Main Hub").build();
        when(warehouseMapper.toResponse(any(Warehouse.class))).thenReturn(mockResponse);

        Page<WarehouseResponse> result = warehouseService.getAllWarehouses(1L, "Hub", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Main Hub", result.getContent().get(0).name());
        verify(warehouseRepository).findByTenantIdAndNameContainingIgnoreCase(1L, "Hub", pageable);
    }

    @Test
    void testCreateWarehouse() {
        WarehouseRequest request = WarehouseRequest.builder().name("West Coast Hub").location("California").build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));

        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(i -> {
            Warehouse w = i.getArgument(0);
            w.setId(20L);
            return w;
        });

        WarehouseResponse mockResponse = WarehouseResponse.builder().id(20L).name("West Coast Hub").build();
        when(warehouseMapper.toResponse(any(Warehouse.class))).thenReturn(mockResponse);

        WarehouseResponse response = warehouseService.createWarehouse(1L, request);

        assertNotNull(response);
        assertEquals(20L, response.id());
        assertEquals("West Coast Hub", response.name());
    }

    @Test
    void testUpdateWarehouse() {
        WarehouseRequest request = WarehouseRequest.builder().name("Updated Hub").location("").build();when(warehouseRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(mockWarehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(i -> i.getArgument(0));

        WarehouseResponse mockResponse = WarehouseResponse.builder().id(10L).name("Updated Hub").build();
        when(warehouseMapper.toResponse(any(Warehouse.class))).thenReturn(mockResponse);

        WarehouseResponse response = warehouseService.updateWarehouse(1L, 10L, request);

        assertNotNull(response);
        assertEquals("Updated Hub", response.name());
    }

    @Test
    void testDeleteWarehouse() {
        mockWarehouse.setIsSystemStore(false);
        when(warehouseRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(mockWarehouse));

        warehouseService.deleteWarehouse(1L, 10L);

        verify(warehouseRepository).delete(mockWarehouse);
    }

    @Test
    void testDeleteWarehouse_SystemStore() {
        mockWarehouse.setIsSystemStore(true);
        when(warehouseRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(mockWarehouse));

        assertThrows(BusinessRuleViolationException.class, () -> warehouseService.deleteWarehouse(1L, 10L));
    }
}
