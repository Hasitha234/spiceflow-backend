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
import com.spiceflow.backend.inventory.dto.request.InventoryTransactionRequest;
import com.spiceflow.backend.inventory.dto.response.InventoryTransactionResponse;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import com.spiceflow.backend.inventory.mapper.InventoryTransactionMapper;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.repository.InventoryTransactionRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InventoryTransactionServiceTest {

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @Mock
    private InventoryItemRepository itemRepository;
    
    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private InventoryTransactionMapper transactionMapper;

    @InjectMocks
    private InventoryTransactionService transactionService;

    private Tenant mockTenant;
    private InventoryItem mockItem;

    @BeforeEach
    void setUp() {
        mockTenant = new Tenant();
        mockTenant.setId(1L);

        Product p = Product.builder().name("Product A").build();
        p.setId(10L);
        
        Warehouse w = Warehouse.builder().name("Warehouse A").build();
        w.setId(20L);

        mockItem = InventoryItem.builder()
                .product(p)
                .warehouse(w)
                .quantityAvailable(100)
                .quantityReserved(10)
                .tenant(mockTenant)
                .build();
        mockItem.setId(100L);
    }

    @Test
    void testRecordTransaction_In() {
        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
            .inventoryItemId(100L)
            .transactionType("IN")
            .quantity(50)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(itemRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mockItem));
        
        when(transactionRepository.save(any(InventoryTransaction.class))).thenAnswer(i -> {
            InventoryTransaction tx = i.getArgument(0);
            tx.setId(200L);
            return tx;
        });

        InventoryTransactionResponse mockResponse = InventoryTransactionResponse.builder()
            .id(200L)
            .transactionType("IN")
            .quantity(50)
            .build();
        when(transactionMapper.toResponse(any(InventoryTransaction.class))).thenReturn(mockResponse);

        InventoryTransactionResponse response = transactionService.recordTransaction(1L, request);

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertEquals("IN", response.transactionType());
        assertEquals(50, response.quantity());
        
        // Assert inventory item was updated
        assertEquals(150, mockItem.getQuantityAvailable());
        verify(itemRepository).save(mockItem);
    }
    
    @Test
    void testRecordTransaction_Out_Success() {
        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
            .inventoryItemId(100L)
            .transactionType("OUT")
            .quantity(50)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(itemRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mockItem));
        
        when(transactionRepository.save(any(InventoryTransaction.class))).thenAnswer(i -> {
            InventoryTransaction tx = i.getArgument(0);
            tx.setId(200L);
            return tx;
        });

        InventoryTransactionResponse mockResponse = InventoryTransactionResponse.builder()
            .id(200L)
            .transactionType("OUT")
            .quantity(50)
            .build();
        when(transactionMapper.toResponse(any(InventoryTransaction.class))).thenReturn(mockResponse);

        InventoryTransactionResponse response = transactionService.recordTransaction(1L, request);

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertEquals(50, mockItem.getQuantityAvailable()); // 100 - 50 = 50
    }
    
    @Test
    void testRecordTransaction_Out_InsufficientQuantity() {
        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
            .inventoryItemId(100L)
            .transactionType("OUT")
            .quantity(150)
            .build(); // Trying to take more than available (100)

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(itemRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mockItem));
        
        assertThrows(BusinessRuleViolationException.class, () -> {
            transactionService.recordTransaction(1L, request);
        });
    }

    @Test
    void testRecordTransaction_Reserve_Success() {
        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
            .inventoryItemId(100L)
            .transactionType("RESERVE")
            .quantity(20)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(itemRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mockItem));
        when(transactionRepository.save(any(InventoryTransaction.class))).thenAnswer(i -> i.getArgument(0));

        InventoryTransactionResponse mockResponse = InventoryTransactionResponse.builder().build();
        when(transactionMapper.toResponse(any(InventoryTransaction.class))).thenReturn(mockResponse);

        transactionService.recordTransaction(1L, request);
        assertEquals(80, mockItem.getQuantityAvailable());
        assertEquals(30, mockItem.getQuantityReserved());
    }

    @Test
    void testRecordTransaction_Unreserve_Success() {
        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
            .inventoryItemId(100L)
            .transactionType("UNRESERVE")
            .quantity(5)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(itemRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mockItem));
        when(transactionRepository.save(any(InventoryTransaction.class))).thenAnswer(i -> i.getArgument(0));

        InventoryTransactionResponse mockResponse = InventoryTransactionResponse.builder().build();
        when(transactionMapper.toResponse(any(InventoryTransaction.class))).thenReturn(mockResponse);

        transactionService.recordTransaction(1L, request);
        assertEquals(105, mockItem.getQuantityAvailable());
        assertEquals(5, mockItem.getQuantityReserved());
    }

    @Test
    void testRecordTransaction_ShipReserved_Success() {
        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
            .inventoryItemId(100L)
            .transactionType("SHIP_RESERVED")
            .quantity(10)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(itemRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mockItem));
        when(transactionRepository.save(any(InventoryTransaction.class))).thenAnswer(i -> i.getArgument(0));

        InventoryTransactionResponse mockResponse = InventoryTransactionResponse.builder().build();
        when(transactionMapper.toResponse(any(InventoryTransaction.class))).thenReturn(mockResponse);

        transactionService.recordTransaction(1L, request);
        assertEquals(100, mockItem.getQuantityAvailable());
        assertEquals(0, mockItem.getQuantityReserved());
    }

    @Test
    void testRecordTransaction_AdjustUp_Success() {
        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
            .inventoryItemId(100L)
            .transactionType("ADJUST_UP")
            .quantity(10)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(itemRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mockItem));
        when(transactionRepository.save(any(InventoryTransaction.class))).thenAnswer(i -> i.getArgument(0));

        InventoryTransactionResponse mockResponse = InventoryTransactionResponse.builder().build();
        when(transactionMapper.toResponse(any(InventoryTransaction.class))).thenReturn(mockResponse);

        transactionService.recordTransaction(1L, request);
        assertEquals(110, mockItem.getQuantityAvailable());
    }

    @Test
    void testRecordTransaction_AdjustDown_Success() {
        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
            .inventoryItemId(100L)
            .transactionType("ADJUST_DOWN")
            .quantity(10)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(itemRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mockItem));
        when(transactionRepository.save(any(InventoryTransaction.class))).thenAnswer(i -> i.getArgument(0));

        InventoryTransactionResponse mockResponse = InventoryTransactionResponse.builder().build();
        when(transactionMapper.toResponse(any(InventoryTransaction.class))).thenReturn(mockResponse);

        transactionService.recordTransaction(1L, request);
        assertEquals(90, mockItem.getQuantityAvailable());
    }

    @Test
    void testRecordTransaction_AdjustDown_Insufficient() {
        InventoryTransactionRequest request = InventoryTransactionRequest.builder()
            .inventoryItemId(100L)
            .transactionType("ADJUST_DOWN")
            .quantity(200)
            .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(itemRepository.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mockItem));

        assertThrows(BusinessRuleViolationException.class, () -> transactionService.recordTransaction(1L, request));
    }
}
