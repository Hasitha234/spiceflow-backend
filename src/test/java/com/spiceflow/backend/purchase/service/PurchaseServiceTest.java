package com.spiceflow.backend.purchase.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Supplier;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.repository.InventoryTransactionRepository;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.inventory.service.ProductService;
import com.spiceflow.backend.inventory.service.SupplierService;
import com.spiceflow.backend.purchase.dto.request.CreatePurchaseRequest;
import com.spiceflow.backend.purchase.dto.request.PurchaseLineItemRequest;
import com.spiceflow.backend.purchase.dto.response.PurchaseResponse;
import com.spiceflow.backend.purchase.entity.Purchase;
import com.spiceflow.backend.purchase.entity.PurchaseLineItem;
import com.spiceflow.backend.purchase.mapper.PurchaseMapper;
import com.spiceflow.backend.purchase.repository.PurchaseLineItemRepository;
import com.spiceflow.backend.purchase.repository.PurchaseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class PurchaseServiceTest {

    @Mock private PurchaseRepository purchaseRepository;
    @Mock private PurchaseLineItemRepository purchaseLineItemRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private SupplierService supplierService;
    @Mock private ProductService productService;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock private PurchaseMapper purchaseMapper;

    @InjectMocks private PurchaseService purchaseService;

    private Tenant tenant;
    private Supplier supplier;
    private Product product;
    private Purchase purchase;
    private PurchaseResponse purchaseResponse;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        supplier = new Supplier();
        supplier.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        purchase = new Purchase();
        purchase.setId(1L);
        purchase.setTenant(tenant);
        purchase.setSupplier(supplier);
        purchase.setInvoiceNo("INV-123");
        purchase.setStatus("DRAFT");
        
        PurchaseLineItem item = new PurchaseLineItem();
        item.setProduct(product);
        item.setSoldQuantity(BigDecimal.valueOf(10));
        purchase.setLineItems(new java.util.ArrayList<>(List.of(item)));
        purchase.setReturnItems(new java.util.ArrayList<>());

        purchaseResponse = PurchaseResponse.builder().id(1L).build();
    }

    @Test
    void createPurchase_Success() {
        PurchaseLineItemRequest lineItem = PurchaseLineItemRequest.builder().productId(1L).soldQuantity(BigDecimal.valueOf(10)).rate(java.math.BigDecimal.TEN).noOfBoxes(BigDecimal.valueOf(1)).build();
        CreatePurchaseRequest request = CreatePurchaseRequest.builder().invoiceNo("INV-123").supplierId(1L).lineItems(java.util.List.of(lineItem)).build();
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(supplierService.getSupplierEntity(1L, 1L)).thenReturn(supplier);
        when(productService.getProductEntity(1L, 1L)).thenReturn(product);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        PurchaseResponse response = purchaseService.createPurchase(1L, request);

        assertNotNull(response);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void createPurchase_Success_WithUnitDivisor() {
        product.setItemsPerSoldUnit(12);
        product.setSoldUnitsPerBox(2);
        
        PurchaseLineItemRequest lineItemDZ = PurchaseLineItemRequest.builder()
                .productId(1L).soldQuantity(BigDecimal.valueOf(10)).rate(java.math.BigDecimal.TEN).noOfBoxes(BigDecimal.valueOf(2)).unitType("DZ").build();
                
        PurchaseLineItemRequest lineItemMC = PurchaseLineItemRequest.builder()
                .productId(1L).soldQuantity(BigDecimal.valueOf(10)).rate(java.math.BigDecimal.TEN).noOfBoxes(BigDecimal.valueOf(2)).unitType("MC").build();
                
        CreatePurchaseRequest request = CreatePurchaseRequest.builder()
                .invoiceNo("INV-124").supplierId(1L).lineItems(java.util.List.of(lineItemDZ, lineItemMC)).build();
                
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(supplierService.getSupplierEntity(1L, 1L)).thenReturn(supplier);
        when(productService.getProductEntity(1L, 1L)).thenReturn(product);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        PurchaseResponse response = purchaseService.createPurchase(1L, request);

        assertNotNull(response);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void createPurchase_TenantNotFound() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> purchaseService.createPurchase(1L, CreatePurchaseRequest.builder().supplierId(1L).build()));
    }

    @Test
    void getPurchases_WithoutInvoiceNo() {
        Page<Purchase> page = new PageImpl<>(List.of(purchase));
        when(purchaseRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        Page<PurchaseResponse> responses = purchaseService.getPurchases(1L, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, responses.getContent().size());
    }

    @Test
    void getPurchases_WithInvoiceNo() {
        Page<Purchase> page = new PageImpl<>(List.of(purchase));
        when(purchaseRepository.findByTenantIdAndInvoiceNoContainingIgnoreCase(eq(1L), eq("INV"), any(PageRequest.class))).thenReturn(page);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        Page<PurchaseResponse> responses = purchaseService.getPurchases(1L, "INV", null, null, PageRequest.of(0, 10));

        assertEquals(1, responses.getContent().size());
    }

    @Test
    void getPurchase_Success() {
        when(purchaseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(purchase));
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        PurchaseResponse response = purchaseService.getPurchase(1L, 1L);

        assertNotNull(response);
    }

    @Test
    void updatePurchase_Success() {
        PurchaseLineItemRequest lineItem = PurchaseLineItemRequest.builder()
                .productId(1L).soldQuantity(BigDecimal.valueOf(10)).rate(java.math.BigDecimal.TEN).noOfBoxes(BigDecimal.valueOf(1)).build();
        CreatePurchaseRequest request = CreatePurchaseRequest.builder()
                .invoiceNo("INV-123-NEW").supplierId(1L).lineItems(java.util.List.of(lineItem))
                .returnItems(java.util.List.of())
                .build();
        
        when(purchaseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(purchase));
        when(supplierService.getSupplierEntity(1L, 1L)).thenReturn(supplier);
        when(productService.getProductEntity(1L, 1L)).thenReturn(product);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        PurchaseResponse response = purchaseService.updatePurchase(1L, 1L, request);

        assertNotNull(response);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void updatePurchase_NotDraft() {
        purchase.setStatus("CONFIRMED");
        when(purchaseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(purchase));
        CreatePurchaseRequest request = CreatePurchaseRequest.builder().build();

        assertThrows(BusinessRuleViolationException.class, () -> purchaseService.updatePurchase(1L, 1L, request));
    }

    @Test
    void confirmPurchase_Success() {
        when(purchaseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(purchase));
        
        Warehouse mainStore = new Warehouse();
        mainStore.setId(1L);
        mainStore.setStoreType("MAIN");
        when(warehouseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(mainStore));

        InventoryItem invItem = new InventoryItem();
        invItem.setId(1L);
        invItem.setQuantityAvailable(50);
        when(inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(1L, 1L, 1L))
                .thenReturn(Optional.of(invItem));
                
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(invItem);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(purchaseMapper.toResponse(purchase)).thenReturn(purchaseResponse);

        PurchaseResponse response = purchaseService.confirmPurchase(1L, 1L, 1L);

        assertNotNull(response);
        assertEquals("CONFIRMED", purchase.getStatus());
        verify(inventoryTransactionRepository, times(1)).save(any());
    }

    @Test
    void confirmPurchase_AlreadyConfirmed() {
        purchase.setStatus("STOCK_UPDATED");
        when(purchaseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(purchase));

        assertThrows(BusinessRuleViolationException.class, () -> purchaseService.confirmPurchase(1L, 1L, 1L));
    }

    @Test
    void confirmPurchase_NoMainStore() {
        when(purchaseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(purchase));
        when(warehouseRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleViolationException.class, 
            () -> purchaseService.confirmPurchase(1L, 1L, 1L));
    }
}
