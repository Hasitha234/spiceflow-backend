package com.spiceflow.backend.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.inventory.dto.request.InventoryItemRequest;
import com.spiceflow.backend.inventory.dto.response.InventoryItemResponse;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
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
public class InventoryItemServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ProductService productService;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private com.spiceflow.backend.inventory.mapper.InventoryItemMapper inventoryItemMapper;

    @InjectMocks
    private InventoryItemService inventoryItemService;

    private Tenant mockTenant;
    private InventoryItem mockItem;
    private Product mockProduct;
    private Warehouse mockWarehouse;

    @BeforeEach
    void setUp() {
        mockTenant = new Tenant();
        mockTenant.setId(1L);

        mockProduct = Product.builder().name("Product A").build();
        mockProduct.setId(10L);

        mockWarehouse = Warehouse.builder().name("Warehouse A").build();
        mockWarehouse.setId(20L);

        mockItem = InventoryItem.builder()
                .product(mockProduct)
                .warehouse(mockWarehouse)
                .quantityAvailable(100)
                .tenant(mockTenant)
                .build();
        mockItem.setId(100L);
    }

    @Test
    void testGetInventoryItems_WithoutSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryItem> page = new PageImpl<>(List.of(mockItem));

        when(inventoryItemRepository.findByTenantId(1L, pageable)).thenReturn(page);

        InventoryItemResponse mockResponse = InventoryItemResponse.builder().id(100L).quantityAvailable(100).build();
        when(inventoryItemMapper.toResponse(any(InventoryItem.class))).thenReturn(mockResponse);

        Page<InventoryItemResponse> responsePage = inventoryItemService.getInventoryItems(1L, null, null, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(100, responsePage.getContent().get(0).getQuantityAvailable());
        verify(inventoryItemRepository).findByTenantId(1L, pageable);
    }

    @Test
    void testCreateInventoryItem_Success() {
        InventoryItemRequest request = new InventoryItemRequest();
        request.setProductId(10L);
        request.setWarehouseId(20L);
        request.setQuantityAvailable(50);
        request.setQuantityReserved(0);
        request.setBatchNumber("BATCH-001");

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(productService.getProductEntity(10L, 1L)).thenReturn(mockProduct);
        when(warehouseService.getWarehouseEntity(20L, 1L)).thenReturn(mockWarehouse);
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(10L, 20L, 1L)).thenReturn(Optional.empty());

        when(inventoryItemRepository.save(any(InventoryItem.class))).thenAnswer(i -> {
            InventoryItem item = i.getArgument(0);
            item.setId(200L);
            return item;
        });

        InventoryItemResponse mockResponse = InventoryItemResponse.builder().id(200L).quantityAvailable(50).build();
        when(inventoryItemMapper.toResponse(any(InventoryItem.class))).thenReturn(mockResponse);

        InventoryItemResponse response = inventoryItemService.createInventoryItem(1L, request);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals(50, response.getQuantityAvailable());
    }
}
