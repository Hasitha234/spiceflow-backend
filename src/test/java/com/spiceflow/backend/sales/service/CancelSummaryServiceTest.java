package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.repository.InventoryTransactionRepository;
import com.spiceflow.backend.inventory.repository.ProductRepository;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.sales.dto.request.CancelSummaryItemRequest;
import com.spiceflow.backend.sales.dto.request.CancelSummaryRequest;
import com.spiceflow.backend.sales.entity.CancelSummary;
import com.spiceflow.backend.sales.entity.CancelSummaryItem;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.mapper.CancelSummaryMapper;
import com.spiceflow.backend.sales.repository.CancelSummaryRepository;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelSummaryServiceTest {

    @Mock
    private CancelSummaryRepository cancelSummaryRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private RepRepository repRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CancelSummaryMapper cancelSummaryMapper;
    @Mock
    private com.spiceflow.backend.sales.repository.DailyBalanceRepository dailyBalanceRepository;
    
    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private InventoryLedgerService inventoryLedgerService;
    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @InjectMocks
    private CancelSummaryService cancelSummaryService;

    private Tenant tenant;
    private Rep rep;
    private Driver driver;
    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        rep = new Rep();
        rep.setId(1L);
        rep.setTenant(tenant);

        driver = new Driver();
        driver.setId(1L);
        driver.setTenant(tenant);

        product = new Product();
        product.setId(1L);
        product.setTenant(tenant);
        // Real-world scenario: ratePerSoldUnit is 0.00 (not null), basePrice has the actual value.
        // resolveUnitPrice must skip zero and fall through to basePrice.
        product.setRatePerSoldUnit(BigDecimal.ZERO);
        product.setBasePrice(BigDecimal.valueOf(100));

        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setTenant(tenant);
    }

    @Test
    void createCancelSummary_Success() {
        // Arrange
        CancelSummaryItemRequest itemRequest = new CancelSummaryItemRequest(1L, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
        CancelSummaryRequest request = new CancelSummaryRequest(1L, 1L, LocalDate.now(), List.of(itemRequest));

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(repRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(rep));
        when(driverRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(driver));
        when(cancelSummaryRepository.findMaxSequenceNumberForDate(eq(1L), any(LocalDate.class))).thenReturn(0);
        when(productRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(product));
        when(cancelSummaryRepository.save(any(CancelSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cancelSummaryService.createCancelSummary(1L, request);

        // Assert — verify server-side price was used
        verify(cancelSummaryRepository).save(argThat(cs ->
                cs.getFinalEstimateValue().compareTo(BigDecimal.valueOf(1000)) == 0
        ));
    }

    @Test
    void createCancelSummary_DuplicateProducts_ThrowsException() {
        // Arrange
        CancelSummaryItemRequest itemRequest1 = new CancelSummaryItemRequest(1L, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
        CancelSummaryItemRequest itemRequest2 = new CancelSummaryItemRequest(1L, 5, BigDecimal.valueOf(100), BigDecimal.valueOf(500));
        CancelSummaryRequest request = new CancelSummaryRequest(1L, 1L, LocalDate.now(), List.of(itemRequest1, itemRequest2));

        // Act & Assert
        assertThatThrownBy(() -> cancelSummaryService.createCancelSummary(1L, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Duplicate products are not allowed in a cancel summary. Please merge quantities for the same product into a single line item.");
    }

    @Test
    void updateCancelSummary_DuplicateProducts_ThrowsException() {
        // Arrange
        CancelSummaryItemRequest itemRequest1 = new CancelSummaryItemRequest(1L, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
        CancelSummaryItemRequest itemRequest2 = new CancelSummaryItemRequest(1L, 5, BigDecimal.valueOf(100), BigDecimal.valueOf(500));
        CancelSummaryRequest request = new CancelSummaryRequest(1L, 1L, LocalDate.now(), List.of(itemRequest1, itemRequest2));

        // Act & Assert
        assertThatThrownBy(() -> cancelSummaryService.updateCancelSummary(1L, 1L, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Duplicate products are not allowed in a cancel summary. Please merge quantities for the same product into a single line item.");
    }

    @Test
    void updateCancelSummary_Success() {
        // Arrange
        CancelSummary summary = new CancelSummary();
        summary.setId(1L);
        summary.setTenant(tenant);
        summary.setStatus("PENDING");
        summary.setSummaryDate(LocalDate.now());

        CancelSummaryItemRequest itemRequest = new CancelSummaryItemRequest(1L, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
        CancelSummaryRequest request = new CancelSummaryRequest(1L, 1L, LocalDate.now(), List.of(itemRequest));

        when(tenantRepository.existsById(1L)).thenReturn(true);
        when(cancelSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(summary));
        when(repRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(rep));
        when(driverRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(driver));
        when(productRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(product));
        when(cancelSummaryRepository.save(any(CancelSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cancelSummaryService.updateCancelSummary(1L, 1L, request);

        // Assert
        verify(entityManager).flush();
        verify(cancelSummaryRepository).save(argThat(cs ->
                cs.getFinalEstimateValue().compareTo(BigDecimal.valueOf(1000)) == 0
        ));
    }

    @Test
    void updateCancelSummary_DateChanged_ThrowsException() {
        // Arrange
        CancelSummary summary = new CancelSummary();
        summary.setId(1L);
        summary.setTenant(tenant);
        summary.setStatus("PENDING");
        summary.setSummaryDate(LocalDate.now().minusDays(1)); // Different date

        CancelSummaryItemRequest itemRequest = new CancelSummaryItemRequest(1L, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
        CancelSummaryRequest request = new CancelSummaryRequest(1L, 1L, LocalDate.now(), List.of(itemRequest));

        when(tenantRepository.existsById(1L)).thenReturn(true);
        when(cancelSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(summary));

        // Act & Assert
        assertThatThrownBy(() -> cancelSummaryService.updateCancelSummary(1L, 1L, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Cannot change the summary date. Please delete this summary and create a new one.");
    }

    @Test
    void createCancelSummary_UsesRatePerSoldUnitWhenPositive() {
        // Arrange — product with a positive ratePerSoldUnit
        Product productWithRate = new Product();
        productWithRate.setId(2L);
        productWithRate.setTenant(tenant);
        productWithRate.setRatePerSoldUnit(BigDecimal.valueOf(50));
        productWithRate.setBasePrice(BigDecimal.valueOf(100));

        CancelSummaryItemRequest itemRequest = new CancelSummaryItemRequest(2L, 10, BigDecimal.ZERO, BigDecimal.ZERO);
        CancelSummaryRequest request = new CancelSummaryRequest(1L, 1L, LocalDate.now(), List.of(itemRequest));

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(repRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(rep));
        when(driverRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(driver));
        when(cancelSummaryRepository.findMaxSequenceNumberForDate(eq(1L), any(LocalDate.class))).thenReturn(0);
        when(productRepository.findByIdAndTenantId(2L, 1L)).thenReturn(Optional.of(productWithRate));
        when(cancelSummaryRepository.save(any(CancelSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cancelSummaryService.createCancelSummary(1L, request);

        // Assert — should use ratePerSoldUnit (50), not basePrice (100)
        verify(cancelSummaryRepository).save(argThat(cs ->
                cs.getFinalEstimateValue().compareTo(BigDecimal.valueOf(500)) == 0
        ));
    }

    @Test
    void proceedCancelSummary_Success() {
        // Arrange
        CancelSummary summary = new CancelSummary();
        summary.setId(1L);
        summary.setTenant(tenant);
        summary.setStatus("PENDING");
        summary.setInventoryProcessed(false);
        summary.setSummaryNumber("CS-20231010-001");
        summary.setSummaryDate(LocalDate.of(2023, 10, 10));
        
        CancelSummaryItem item = new CancelSummaryItem();
        item.setProduct(product);
        item.setQuantity(10);
        item.setUnitPrice(BigDecimal.valueOf(100));
        summary.addItem(item);

        when(cancelSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(summary));
        when(warehouseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(warehouse));
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setQuantityAvailable(50);
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L))
                .thenReturn(Optional.of(inventoryItem));

        // Act
        cancelSummaryService.proceedCancelSummary(1L, 1L, 1L);

        // Assert
        assertThat(summary.getStatus()).isEqualTo("SETTLED");
        assertThat(summary.isInventoryProcessed()).isTrue();
        assertThat(summary.getReturnWarehouse()).isEqualTo(warehouse);
        assertThat(inventoryItem.getQuantityAvailable()).isEqualTo(60);
        
        verify(inventoryItemRepository).save(inventoryItem);
        verify(inventoryTransactionRepository).save(any());
        verify(inventoryLedgerService).recordMovement(
                eq(1L), eq(1L), eq(1L), eq(InventoryMovementType.CANCEL_RETURN_RECEIPT), 
                eq(BigDecimal.valueOf(10)), eq(BigDecimal.valueOf(100)), eq("CS-20231010-001"), 
                anyString(), isNull(), any(), anyString()
        );
        verify(cancelSummaryRepository).save(summary);
    }

    @Test
    void proceedCancelSummary_InventoryAlreadyProcessed_ThrowsException() {
        // Arrange
        CancelSummary summary = new CancelSummary();
        summary.setInventoryProcessed(true);

        when(cancelSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(summary));

        // Act & Assert
        assertThatThrownBy(() -> cancelSummaryService.proceedCancelSummary(1L, 1L, 1L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Inventory has already been processed for this cancel summary");
    }

    @Test
    void undoProceedCancelSummary_Success() {
        // Arrange
        CancelSummary summary = new CancelSummary();
        summary.setId(1L);
        summary.setTenant(tenant);
        summary.setStatus("SETTLED");
        summary.setInventoryProcessed(true);
        summary.setSummaryNumber("CS-20231010-001");
        summary.setSummaryDate(LocalDate.of(2023, 10, 10));
        summary.setReturnWarehouse(warehouse);
        
        CancelSummaryItem item = new CancelSummaryItem();
        item.setProduct(product);
        item.setQuantity(10);
        item.setUnitPrice(BigDecimal.valueOf(100));
        summary.addItem(item);

        when(cancelSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(summary));
        when(dailyBalanceRepository.existsByTenantIdAndBalanceDate(1L, LocalDate.of(2023, 10, 10))).thenReturn(false);
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setQuantityAvailable(60);
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L))
                .thenReturn(Optional.of(inventoryItem));

        // Act
        cancelSummaryService.undoProceedCancelSummary(1L, 1L);

        // Assert
        assertThat(summary.getStatus()).isEqualTo("PENDING");
        assertThat(summary.isInventoryProcessed()).isFalse();
        assertThat(summary.getReturnWarehouse()).isNull();
        assertThat(inventoryItem.getQuantityAvailable()).isEqualTo(50);
        
        verify(inventoryItemRepository).save(inventoryItem);
        verify(inventoryTransactionRepository).save(any());
        verify(inventoryLedgerService).recordMovement(
                eq(1L), eq(1L), eq(1L), eq(InventoryMovementType.CANCEL_RETURN_REVERSAL), 
                eq(BigDecimal.valueOf(10)), eq(BigDecimal.valueOf(100)), eq("CS-20231010-001-REVERSAL"), 
                anyString(), isNull(), any(), anyString()
        );
        verify(cancelSummaryRepository).save(summary);
    }

    @Test
    void undoProceedCancelSummary_InventoryNotProcessed_ThrowsException() {
        // Arrange
        CancelSummary summary = new CancelSummary();
        summary.setInventoryProcessed(false);

        when(cancelSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(summary));

        // Act & Assert
        assertThatThrownBy(() -> cancelSummaryService.undoProceedCancelSummary(1L, 1L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Cannot undo: inventory has not been processed for this cancel summary");
    }
}
