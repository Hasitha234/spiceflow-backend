package com.spiceflow.backend.sales.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.sales.dto.request.DriverRequest;
import com.spiceflow.backend.sales.dto.request.RepRequest;
import com.spiceflow.backend.sales.dto.request.ShopRequest;
import com.spiceflow.backend.sales.dto.response.DriverResponse;
import com.spiceflow.backend.sales.dto.response.RepResponse;
import com.spiceflow.backend.sales.dto.response.ShopResponse;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.mapper.SalesMapper;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
import java.math.BigDecimal;
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
class SalesMasterDataServiceTest {

    @Mock private ShopRepository shopRepository;
    @Mock private RepRepository repRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private SalesMapper salesMapper;

    @InjectMocks private SalesMasterDataService salesMasterDataService;

    private Tenant tenant;
    private Rep rep;
    private Driver driver;
    private Shop shop;
    @Mock private RepResponse repResponse;
    @Mock private DriverResponse driverResponse;
    @Mock private ShopResponse shopResponse;

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

        shop = new Shop();
        shop.setId(1L);
        shop.setTenant(tenant);
    }

    @Test
    void createRep_Success() {
        RepRequest request = new RepRequest();
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(repRepository.save(any(Rep.class))).thenReturn(rep);
        when(salesMapper.toRepResponse(rep)).thenReturn(repResponse);

        RepResponse result = salesMasterDataService.createRep(1L, request);

        assertNotNull(result);
    }

    @Test
    void getReps_Success() {
        Page<Rep> page = new PageImpl<>(List.of(rep));
        when(repRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(salesMapper.toRepResponse(rep)).thenReturn(repResponse);

        Page<RepResponse> result = salesMasterDataService.getReps(1L, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }
    
    @Test
    void getReps_WithSearch() {
        Page<Rep> page = new PageImpl<>(List.of(rep));
        when(repRepository.findByTenantIdAndNameContainingIgnoreCase(eq(1L), eq("test"), any(PageRequest.class))).thenReturn(page);
        when(salesMapper.toRepResponse(rep)).thenReturn(repResponse);

        Page<RepResponse> result = salesMasterDataService.getReps(1L, "test", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void createDriver_Success() {
        DriverRequest request = new DriverRequest();
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(salesMapper.toDriverResponse(driver)).thenReturn(driverResponse);

        DriverResponse result = salesMasterDataService.createDriver(1L, request);

        assertNotNull(result);
    }

    @Test
    void getDrivers_Success() {
        Page<Driver> page = new PageImpl<>(List.of(driver));
        when(driverRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(salesMapper.toDriverResponse(driver)).thenReturn(driverResponse);

        Page<DriverResponse> result = salesMasterDataService.getDrivers(1L, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getDrivers_WithSearch() {
        Page<Driver> page = new PageImpl<>(List.of(driver));
        when(driverRepository.findByTenantIdAndNameContainingIgnoreCase(eq(1L), eq("test"), any(PageRequest.class))).thenReturn(page);
        when(salesMapper.toDriverResponse(driver)).thenReturn(driverResponse);

        Page<DriverResponse> result = salesMasterDataService.getDrivers(1L, "test", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void createShop_Success() {
        ShopRequest request = new ShopRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "assignedRepId", 1L);

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(repRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(rep));
        when(shopRepository.save(any(Shop.class))).thenReturn(shop);
        when(salesMapper.toShopResponse(shop)).thenReturn(shopResponse);

        ShopResponse result = salesMasterDataService.createShop(1L, request);

        assertNotNull(result);
    }

    @Test
    void getShops_Success() {
        Page<Shop> page = new PageImpl<>(List.of(shop));
        when(shopRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(salesMapper.toShopResponse(shop)).thenReturn(shopResponse);

        Page<ShopResponse> result = salesMasterDataService.getShops(1L, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getShops_WithSearch() {
        Page<Shop> page = new PageImpl<>(List.of(shop));
        when(shopRepository.findByTenantIdAndNameContainingIgnoreCase(eq(1L), eq("test"), any(PageRequest.class))).thenReturn(page);
        when(salesMapper.toShopResponse(shop)).thenReturn(shopResponse);

        Page<ShopResponse> result = salesMasterDataService.getShops(1L, "test", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getShopEntity_Success() {
        when(shopRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(shop));
        Shop result = salesMasterDataService.getShopEntity(1L, 1L);
        assertNotNull(result);
    }
    
    @Test
    void getShopEntity_NotFound() {
        when(shopRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> salesMasterDataService.getShopEntity(1L, 1L));
    }

    @Test
    void getRepEntity_Success() {
        when(repRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(rep));
        Rep result = salesMasterDataService.getRepEntity(1L, 1L);
        assertNotNull(result);
    }

    @Test
    void getDriverEntity_Success() {
        when(driverRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(driver));
        Driver result = salesMasterDataService.getDriverEntity(1L, 1L);
        assertNotNull(result);
    }
}
