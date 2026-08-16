package com.spiceflow.backend.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.InventoryBatchTransferRequest;
import com.spiceflow.backend.inventory.dto.response.InventoryBatchTransferResponse;
import com.spiceflow.backend.inventory.dto.request.InventoryItemRequest;
import com.spiceflow.backend.inventory.dto.request.InventoryMarkDamagedRequest;
import com.spiceflow.backend.inventory.dto.request.InventoryTransferRequest;
import com.spiceflow.backend.inventory.dto.response.InventoryItemResponse;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.mapper.InventoryItemMapper;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.repository.InventoryTransactionRepository;
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
class InventoryItemServiceTest {

    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private ProductService productService;
    @Mock private WarehouseService warehouseService;
    @Mock private InventoryItemMapper inventoryItemMapper;
    @Mock private InventoryLedgerService inventoryLedgerService;

    @InjectMocks private InventoryItemService inventoryItemService;

    private Tenant tenant;
    private Product product;
    private Warehouse warehouse;
    private InventoryItem inventoryItem;
    @Mock private InventoryItemResponse response;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        product = new Product();
        product.setId(1L);

        warehouse = new Warehouse();
        warehouse.setId(1L);

        inventoryItem = new InventoryItem();
        inventoryItem.setId(1L);
        inventoryItem.setTenant(tenant);
        inventoryItem.setProduct(product);
        inventoryItem.setWarehouse(warehouse);
        inventoryItem.setQuantityAvailable(100);
        inventoryItem.setQuantityReserved(10);
    }

    @Test
    void createInventoryItem_Success() {
        InventoryItemRequest request = InventoryItemRequest.builder()
            .productId(1L)
            .warehouseId(1L)
            .quantityAvailable(50)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L)).thenReturn(Optional.empty());
        when(productService.getProductEntity(1L, 1L)).thenReturn(product);
        when(warehouseService.getWarehouseEntity(1L, 1L)).thenReturn(warehouse);
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(inventoryItem);
        when(inventoryItemMapper.toResponse(inventoryItem)).thenReturn(response);

        InventoryItemResponse result = inventoryItemService.createInventoryItem(1L, request);

        assertNotNull(result);
        verify(inventoryItemRepository).save(any(InventoryItem.class));
    }

    @Test
    void createInventoryItem_AlreadyExists() {
        InventoryItemRequest request = InventoryItemRequest.builder()
            .productId(1L)
            .warehouseId(1L)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L)).thenReturn(Optional.of(inventoryItem));

        assertThrows(BusinessRuleViolationException.class, () -> inventoryItemService.createInventoryItem(1L, request));
    }

    @Test
    void getInventoryItems_WithoutFilters() {
        Page<InventoryItem> page = new PageImpl<>(List.of(inventoryItem));
        when(inventoryItemRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(inventoryItemMapper.toResponse(inventoryItem)).thenReturn(response);

        Page<InventoryItemResponse> result = inventoryItemService.getInventoryItems(1L, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getInventoryItems_WithFilters() {
        Page<InventoryItem> page = new PageImpl<>(List.of(inventoryItem));
        when(inventoryItemRepository.findByWarehouseIdAndProductIdAndTenantId(eq(1L), eq(1L), eq(1L), any(PageRequest.class))).thenReturn(page);
        when(inventoryItemMapper.toResponse(inventoryItem)).thenReturn(response);

        Page<InventoryItemResponse> result = inventoryItemService.getInventoryItems(1L, 1L, 1L, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void updateInventoryItem_Success() {
        InventoryItemRequest request = InventoryItemRequest.builder()
            .quantityAvailable(150)
            .build();

        when(inventoryItemRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(inventoryItem);
        when(inventoryItemMapper.toResponse(inventoryItem)).thenReturn(response);

        InventoryItemResponse result = inventoryItemService.updateInventoryItem(1L, 1L, request);

        assertNotNull(result);
        assertEquals(150, inventoryItem.getQuantityAvailable());
    }

    @Test
    void deleteInventoryItem_Success() {
        inventoryItem.setQuantityAvailable(0);
        inventoryItem.setQuantityReserved(0);
        when(inventoryItemRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(inventoryItem));

        inventoryItemService.deleteInventoryItem(1L, 1L);

        verify(inventoryItemRepository).delete(inventoryItem);
    }

    @Test
    void deleteInventoryItem_NonZeroQuantity() {
        when(inventoryItemRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(inventoryItem));

        assertThrows(BusinessRuleViolationException.class, () -> inventoryItemService.deleteInventoryItem(1L, 1L));
    }

    @Test
    void transferInventory_Success() {
        InventoryTransferRequest request = InventoryTransferRequest.builder()
            .productId(1L)
            .fromWarehouseId(1L)
            .toWarehouseId(2L)
            .quantity(50)
            .reason("Transfer")
            .build();

        InventoryItem destItem = new InventoryItem();
        destItem.setQuantityAvailable(10);

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 2L, 1L)).thenReturn(Optional.of(destItem));
        when(inventoryItemRepository.save(any())).thenReturn(destItem);

        inventoryItemService.transferInventory(1L, request);

        assertEquals(50, inventoryItem.getQuantityAvailable());
        assertEquals(60, destItem.getQuantityAvailable());
        verify(inventoryTransactionRepository, times(2)).save(any(InventoryTransaction.class));
    }

    @Test
    void markDamaged_Success() {
        InventoryMarkDamagedRequest request = InventoryMarkDamagedRequest.builder()
            .productId(1L)
            .warehouseId(1L)
            .quantity(20)
            .notes("Damaged")
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L)).thenReturn(Optional.of(inventoryItem));

        inventoryItemService.markDamaged(1L, request);

        assertEquals(80, inventoryItem.getQuantityAvailable());
        verify(inventoryTransactionRepository).save(any(InventoryTransaction.class));
    }

    @Test
    void batchTransfer_Success() {
        Warehouse toWarehouse = new Warehouse();
        toWarehouse.setId(2L);

        InventoryItem destItem = new InventoryItem();
        destItem.setId(2L);
        destItem.setTenant(tenant);
        destItem.setProduct(product);
        destItem.setWarehouse(toWarehouse);
        destItem.setQuantityAvailable(50);

        InventoryBatchTransferRequest.TransferLineItem itemRequest = new InventoryBatchTransferRequest.TransferLineItem(
            1L, 10
        );

        InventoryBatchTransferRequest request = new InventoryBatchTransferRequest(
            1L, 2L, List.of(itemRequest), "Test transfer notes"
        );

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(warehouseService.getWarehouseEntity(1L, 1L)).thenReturn(warehouse);
        when(warehouseService.getWarehouseEntity(1L, 2L)).thenReturn(toWarehouse);
        when(productService.getProductEntity(1L, 1L)).thenReturn(product);
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L))
            .thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 2L, 1L))
            .thenReturn(Optional.of(destItem));

        InventoryBatchTransferResponse result = inventoryItemService.batchTransfer(1L, "test_user", request);

        assertNotNull(result);
        assertEquals(1, result.transferredItems().size());
        assertEquals(90, inventoryItem.getQuantityAvailable());
        assertEquals(60, destItem.getQuantityAvailable());
        verify(inventoryTransactionRepository, times(2)).save(any(InventoryTransaction.class));
    }

    @Test
    void batchTransfer_SameWarehouse_ThrowsException() {
        InventoryBatchTransferRequest request = new InventoryBatchTransferRequest(
            1L, 1L, List.of(), "Test transfer notes"
        );

        assertThrows(BusinessRuleViolationException.class, () -> {
            inventoryItemService.batchTransfer(1L, "test_user", request);
        });
    }
}
