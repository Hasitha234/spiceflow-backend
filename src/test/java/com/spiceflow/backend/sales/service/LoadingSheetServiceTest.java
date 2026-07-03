package com.spiceflow.backend.sales.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.inventory.dto.request.InventoryTransferRequest;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.inventory.service.InventoryItemService;
import com.spiceflow.backend.sales.dto.request.CreateLoadingSheetRequest;
import com.spiceflow.backend.sales.dto.response.LoadingSheetResponse;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.LoadingSheet;
import com.spiceflow.backend.sales.entity.LoadingSheetItem;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.entity.RepOrderItem;
import com.spiceflow.backend.sales.entity.RepOrderShop;
import com.spiceflow.backend.sales.entity.ShopReturn;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.sales.mapper.LoadingSheetMapper;
import com.spiceflow.backend.sales.repository.LoadingSheetRepository;
import com.spiceflow.backend.sales.repository.RepOrderRepository;
import java.time.LocalDate;
import java.util.ArrayList;
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
class LoadingSheetServiceTest {

    @Mock private LoadingSheetRepository loadingSheetRepository;
    @Mock private RepOrderRepository repOrderRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private SalesMasterDataService salesMasterDataService;
    @Mock private LoadingSheetMapper loadingSheetMapper;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private InventoryItemService inventoryItemService;

    @InjectMocks private LoadingSheetService loadingSheetService;

    private Tenant tenant;
    private RepOrder repOrder;
    private Driver driver;
    private LoadingSheet loadingSheet;
    @Mock private LoadingSheetResponse loadingSheetResponse;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        repOrder = new RepOrder();
        repOrder.setId(1L);
        repOrder.setTenant(tenant);
        repOrder.setLoadingStatus("DRAFT");
        
        Product product = new Product();
        product.setId(1L);
        
        RepOrderItem item = new RepOrderItem();
        item.setProduct(product);
        item.setQuantity(10);
        item.setUnitType("BOX");

        ShopReturn sr = new ShopReturn();
        sr.setProduct(product);
        sr.setQuantity(2);
        sr.setReturnType("DAMAGED");

        RepOrderShop shop = new RepOrderShop();
        shop.setItems(List.of(item));
        shop.setReturns(List.of(sr));

        repOrder.setShops(List.of(shop));

        driver = new Driver();
        driver.setId(1L);
        driver.setName("John Driver");

        loadingSheet = new LoadingSheet();
        loadingSheet.setId(1L);
        loadingSheet.setTenant(tenant);
        loadingSheet.setRepOrder(repOrder);
        loadingSheet.setDriver(driver);
        loadingSheet.setStatus("DRAFT");
    }

    @Test
    void createLoadingSheet_Success() {
        CreateLoadingSheetRequest request = CreateLoadingSheetRequest.builder().repOrderId(1L).driverId(1L).loadingDate(java.time.LocalDate.now()).build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(repOrderRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(repOrder));
        when(salesMasterDataService.getDriverEntity(1L, 1L)).thenReturn(driver);
        when(loadingSheetRepository.save(any(LoadingSheet.class))).thenReturn(loadingSheet);
        when(loadingSheetMapper.toResponse(loadingSheet)).thenReturn(loadingSheetResponse);

        LoadingSheetResponse result = loadingSheetService.createLoadingSheet(1L, request);

        assertNotNull(result);
        assertEquals("IN_PROGRESS", repOrder.getLoadingStatus());
        verify(repOrderRepository).save(repOrder);
        verify(loadingSheetRepository).save(any(LoadingSheet.class));
    }

    @Test
    void createLoadingSheet_AlreadyLoaded() {
        repOrder.setLoadingStatus("IN_PROGRESS");
        CreateLoadingSheetRequest request = CreateLoadingSheetRequest.builder().repOrderId(1L).driverId(1L).loadingDate(java.time.LocalDate.now()).build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(repOrderRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(repOrder));

        assertThrows(BusinessRuleViolationException.class, () -> loadingSheetService.createLoadingSheet(1L, request));
    }

    @Test
    void confirmLoadingSheet_Success_ExistingVehicleStore() {
        Product p = new Product();
        p.setId(1L);

        LoadingSheetItem sheetItem = new LoadingSheetItem();
        sheetItem.setProduct(p);
        sheetItem.setQuantityLoaded(10);
        loadingSheet.setItems(List.of(sheetItem));

        Warehouse mainStore = new Warehouse();
        mainStore.setId(1L);
        mainStore.setStoreType("MAIN");

        Warehouse vehicleStore = new Warehouse();
        vehicleStore.setId(2L);
        vehicleStore.setStoreType("CUSTOM");
        vehicleStore.setName("Vehicle - John Driver");

        when(loadingSheetRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(loadingSheet));
        when(warehouseRepository.findAllByTenantId(1L)).thenReturn(List.of(mainStore, vehicleStore));
        when(loadingSheetRepository.save(any(LoadingSheet.class))).thenReturn(loadingSheet);
        when(loadingSheetMapper.toResponse(loadingSheet)).thenReturn(loadingSheetResponse);

        LoadingSheetResponse result = loadingSheetService.confirmLoadingSheet(1L, 1L);

        assertNotNull(result);
        assertEquals("CONFIRMED", loadingSheet.getStatus());
        assertEquals("LOADED", repOrder.getLoadingStatus());
        verify(inventoryItemService).transferInventory(eq(1L), any(InventoryTransferRequest.class));
    }

    @Test
    void confirmLoadingSheet_Success_CreateVehicleStore() {
        Product p = new Product();
        p.setId(1L);

        LoadingSheetItem sheetItem = new LoadingSheetItem();
        sheetItem.setProduct(p);
        sheetItem.setQuantityLoaded(10);
        loadingSheet.setItems(List.of(sheetItem));

        Warehouse mainStore = new Warehouse();
        mainStore.setId(1L);
        mainStore.setStoreType("MAIN");

        Warehouse vehicleStore = new Warehouse();
        vehicleStore.setId(2L);
        vehicleStore.setStoreType("CUSTOM");
        vehicleStore.setName("Vehicle - John Driver");

        when(loadingSheetRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(loadingSheet));
        when(warehouseRepository.findAllByTenantId(1L)).thenReturn(List.of(mainStore));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(vehicleStore);
        when(loadingSheetRepository.save(any(LoadingSheet.class))).thenReturn(loadingSheet);
        when(loadingSheetMapper.toResponse(loadingSheet)).thenReturn(loadingSheetResponse);

        LoadingSheetResponse result = loadingSheetService.confirmLoadingSheet(1L, 1L);

        assertNotNull(result);
        assertEquals("CONFIRMED", loadingSheet.getStatus());
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void confirmLoadingSheet_NotDraft() {
        loadingSheet.setStatus("CONFIRMED");
        when(loadingSheetRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(loadingSheet));

        assertThrows(BusinessRuleViolationException.class, () -> loadingSheetService.confirmLoadingSheet(1L, 1L));
    }

    @Test
    void getLoadingSheets_Success() {
        Page<LoadingSheet> page = new PageImpl<>(List.of(loadingSheet));
        when(loadingSheetRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(loadingSheetMapper.toResponse(loadingSheet)).thenReturn(loadingSheetResponse);

        Page<LoadingSheetResponse> result = loadingSheetService.getLoadingSheets(1L, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getLoadingSheet_Success() {
        when(loadingSheetRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(loadingSheet));
        when(loadingSheetMapper.toResponse(loadingSheet)).thenReturn(loadingSheetResponse);

        LoadingSheetResponse result = loadingSheetService.getLoadingSheet(1L, 1L);
        assertNotNull(result);
    }
}
