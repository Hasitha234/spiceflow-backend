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
import com.spiceflow.backend.sales.dto.request.EveningSummaryItemRequest;
import com.spiceflow.backend.sales.dto.request.EveningSummaryRequest;
import com.spiceflow.backend.sales.dto.response.EveningSummaryResponse;
import com.spiceflow.backend.sales.dto.response.StockAvailabilityResponse;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.EveningSummary;
import com.spiceflow.backend.sales.entity.EveningSummaryItem;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.mapper.EveningSummaryMapper;
import com.spiceflow.backend.sales.repository.DailyBalanceRepository;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.sales.repository.EveningSummaryRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import jakarta.persistence.EntityManager;
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
class EveningSummaryServiceTest {

    @Mock private EveningSummaryRepository eveningSummaryRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private RepRepository repRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private ProductRepository productRepository;
    @Mock private EveningSummaryMapper eveningSummaryMapper;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private InventoryLedgerService inventoryLedgerService;
    @Mock private DailyBalanceRepository dailyBalanceRepository;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private EveningSummaryService eveningSummaryService;

    private Tenant tenant;
    private Rep rep;
    private Driver driver;
    private Product product;
    private Warehouse warehouse;
    private EveningSummary eveningSummary;
    private EveningSummaryRequest createRequest;
    private EveningSummaryResponse summaryResponse;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        rep = new Rep();
        rep.setId(1L);
        rep.setTenant(tenant);
        rep.setName("Test Rep");

        driver = new Driver();
        driver.setId(1L);
        driver.setTenant(tenant);
        driver.setName("Test Driver");

        product = new Product();
        product.setId(1L);
        product.setTenant(tenant);
        product.setName("Test Product");
        product.setSku("PRD-001");
        product.setBasePrice(new BigDecimal("100.00"));

        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setTenant(tenant);
        warehouse.setName("Main Warehouse");

        eveningSummary = EveningSummary.builder()
                .tenant(tenant)
                .rep(rep)
                .driver(driver)
                .summaryDate(LocalDate.now())
                .summaryNumber("ES-001")
                .status("PENDING")
                .items(new ArrayList<>())
                .build();
        eveningSummary.setId(1L);

        EveningSummaryItem item = EveningSummaryItem.builder()
                .eveningSummary(eveningSummary)
                .product(product)
                .quantity(5)
                .unitPrice(new BigDecimal("100.00"))
                .estimateValue(new BigDecimal("500.00"))
                .build();
        item.setId(1L);
        eveningSummary.getItems().add(item);

        createRequest = new EveningSummaryRequest(
                1L, 1L, LocalDate.now(),
                List.of(new EveningSummaryItemRequest(1L, 5, new BigDecimal("100.00"), new BigDecimal("500.00")))
        );

        summaryResponse = new EveningSummaryResponse(
                1L, 1L, 1L, "Test Rep", 1L, "Test Driver", LocalDate.now(), "ES-001", new BigDecimal("500.00"),
                "PENDING", false, null, null, null, null, null, null, null
        );
    }

    @Test
    void createEveningSummary_Success() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(repRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(rep));
        when(driverRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(driver));
        when(productRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(product));
        when(eveningSummaryRepository.save(any(EveningSummary.class))).thenReturn(eveningSummary);
        when(eveningSummaryMapper.toResponse(any(EveningSummary.class))).thenReturn(summaryResponse);

        EveningSummaryResponse response = eveningSummaryService.createEveningSummary(1L, createRequest);

        assertThat(response).isNotNull();
        assertThat(response.summaryNumber()).isEqualTo("ES-001");
        verify(eveningSummaryRepository).save(any(EveningSummary.class));
    }

    @Test
    void createEveningSummary_DuplicateProduct() {
        EveningSummaryRequest request = new EveningSummaryRequest(
                1L, 1L, LocalDate.now(),
                List.of(
                        new EveningSummaryItemRequest(1L, 5, new BigDecimal("100.00"), new BigDecimal("500.00")),
                        new EveningSummaryItemRequest(1L, 2, new BigDecimal("100.00"), new BigDecimal("200.00"))
                )
        );

        assertThatThrownBy(() -> eveningSummaryService.createEveningSummary(1L, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Duplicate products are not allowed");
    }

    @Test
    void updateEveningSummary_Success() {
        when(tenantRepository.existsById(1L)).thenReturn(true);
        when(eveningSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(eveningSummary));
        when(repRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(rep));
        when(driverRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(driver));
        when(productRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(product));
        when(eveningSummaryRepository.save(any(EveningSummary.class))).thenReturn(eveningSummary);
        when(eveningSummaryMapper.toResponse(any(EveningSummary.class))).thenReturn(summaryResponse);

        EveningSummaryResponse response = eveningSummaryService.updateEveningSummary(1L, 1L, createRequest);

        assertThat(response).isNotNull();
        verify(eveningSummaryRepository).save(any(EveningSummary.class));
        verify(entityManager).flush();
    }

    @Test
    void deleteEveningSummary_Success() {
        when(eveningSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(eveningSummary));

        eveningSummaryService.deleteEveningSummary(1L, 1L);

        verify(eveningSummaryRepository).delete(eveningSummary);
    }

    @Test
    void getEveningSummaries_Success() {
        Page<EveningSummary> page = new PageImpl<>(List.of(eveningSummary));
        when(eveningSummaryRepository.findByFilters(eq(1L), any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(eveningSummaryMapper.toResponse(any(EveningSummary.class))).thenReturn(summaryResponse);

        Page<EveningSummaryResponse> result = eveningSummaryService.getEveningSummaries(1L, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getEveningSummaryById_Success() {
        when(eveningSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(eveningSummary));
        when(eveningSummaryMapper.toResponse(any(EveningSummary.class))).thenReturn(summaryResponse);

        EveningSummaryResponse result = eveningSummaryService.getEveningSummaryById(1L, 1L);

        assertThat(result).isNotNull();
    }

    @Test
    void checkStockAvailability_Success() {
        when(eveningSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(eveningSummary));
        when(warehouseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(warehouse));
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setQuantityAvailable(10);
        
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L)).thenReturn(Optional.of(inventoryItem));

        List<StockAvailabilityResponse> result = eveningSummaryService.checkStockAvailability(1L, 1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sufficient()).isTrue();
    }

    @Test
    void proceedEveningSummary_Success() {
        when(eveningSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(eveningSummary));
        when(warehouseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(warehouse));
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(1L);
        inventoryItem.setWarehouse(warehouse);
        inventoryItem.setProduct(product);
        inventoryItem.setQuantityAvailable(10);
        
        // Mocking checkStockAvailability behavior inline
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L)).thenReturn(Optional.of(inventoryItem));

        eveningSummaryService.proceedEveningSummary(1L, 1L, 1L);

        verify(inventoryItemRepository).save(inventoryItem);
        assertThat(inventoryItem.getQuantityAvailable()).isEqualTo(5);
        assertThat(eveningSummary.isInventoryProcessed()).isTrue();
        assertThat(eveningSummary.getDeductionWarehouse()).isEqualTo(warehouse);
    }
    
    @Test
    void undoProceedEveningSummary_Success() {
        eveningSummary.setInventoryProcessed(true);
        eveningSummary.setDeductionWarehouse(warehouse);
        eveningSummary.setStatus("SETTLED");
        
        when(eveningSummaryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(eveningSummary));
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(1L);
        inventoryItem.setWarehouse(warehouse);
        inventoryItem.setProduct(product);
        inventoryItem.setQuantityAvailable(5);
        
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L)).thenReturn(Optional.of(inventoryItem));

        eveningSummaryService.undoProceedEveningSummary(1L, 1L);

        verify(inventoryItemRepository).save(inventoryItem);
        assertThat(inventoryItem.getQuantityAvailable()).isEqualTo(10);
        assertThat(eveningSummary.isInventoryProcessed()).isFalse();
        assertThat(eveningSummary.getDeductionWarehouse()).isNull();
        assertThat(eveningSummary.getStatus()).isEqualTo("PENDING");
    }
}
