package com.spiceflow.backend.sales.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.sales.dto.response.RepPerformanceResponse;
import com.spiceflow.backend.sales.dto.response.SalesSummaryResponse;
import com.spiceflow.backend.sales.dto.response.ShopOutstandingResponse;
import com.spiceflow.backend.sales.dto.response.StockStatusResponse;
import com.spiceflow.backend.sales.entity.Delivery;
import com.spiceflow.backend.sales.entity.DeliveryShop;
import com.spiceflow.backend.sales.entity.LoadingSheet;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.repository.DeliveryRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private ShopRepository shopRepository;
    @Mock private RepRepository repRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;

    @InjectMocks private ReportService reportService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getSalesSummary_Success() {
        Delivery delivery = new Delivery();
        delivery.setTotalSalesValue(new BigDecimal("1000"));
        delivery.setTotalReturnsValue(new BigDecimal("100"));
        delivery.setTotalCollectedAmount(new BigDecimal("800"));

        DeliveryShop ds = new DeliveryShop();
        ds.setTotalDiscount(new BigDecimal("50"));
        delivery.setShops(List.of(ds));

        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();

        when(deliveryRepository.findDeliveriesInDateRange(1L, start, end)).thenReturn(List.of(delivery));

        SalesSummaryResponse result = reportService.getSalesSummary(1L, start, end).join();

        assertNotNull(result);
        assertEquals(new BigDecimal("1000"), result.totalSales());
        assertEquals(new BigDecimal("100"), result.totalReturns());
        assertEquals(new BigDecimal("900"), result.netSales());
        assertEquals(new BigDecimal("800"), result.totalCollected());
        assertEquals(new BigDecimal("100"), result.totalCreditGiven());
    }

    @Test
    void getShopOutstandings_Success() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setName("Test Shop");
        shop.setOutstandingLoan(new BigDecimal("500"));

        when(shopRepository.findByTenantId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(shop)));

        List<ShopOutstandingResponse> result = reportService.getShopOutstandings(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("500"), result.get(0).outstandingAmount());
    }

    @Test
    void getStockStatus_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Product 1");

        Warehouse mainWarehouse = new Warehouse();
        mainWarehouse.setStoreType("MAIN");

        Warehouse vehicleWarehouse = new Warehouse();
        vehicleWarehouse.setStoreType("CUSTOM");

        InventoryItem item1 = new InventoryItem();
        item1.setProduct(product);
        item1.setWarehouse(mainWarehouse);
        item1.setQuantityAvailable(100);

        InventoryItem item2 = new InventoryItem();
        item2.setProduct(product);
        item2.setWarehouse(vehicleWarehouse);
        item2.setQuantityAvailable(50);

        when(inventoryItemRepository.findByTenantId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item1, item2)));

        List<StockStatusResponse> result = reportService.getStockStatus(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).mainStoreQuantity());
        assertEquals(50, result.get(0).otherStoresQuantity());
        assertEquals(150, result.get(0).totalQuantity());
    }

    @Test
    void getRepPerformance_Success() {
        Rep rep = new Rep();
        rep.setId(1L);
        rep.setName("Rep 1");

        RepOrder repOrder = new RepOrder();
        repOrder.setRep(rep);

        LoadingSheet loadingSheet = new LoadingSheet();
        loadingSheet.setRepOrder(repOrder);

        Delivery delivery = new Delivery();
        delivery.setLoadingSheet(loadingSheet);
        delivery.setTotalSalesValue(new BigDecimal("2000"));
        delivery.setTotalCollectedAmount(new BigDecimal("1800"));

        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();

        when(deliveryRepository.findDeliveriesInDateRange(1L, start, end)).thenReturn(List.of(delivery));

        List<RepPerformanceResponse> result = reportService.getRepPerformance(1L, start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).repId());
        assertEquals(1, result.get(0).totalOrders());
        assertEquals(new BigDecimal("2000"), result.get(0).totalSalesValue());
        assertEquals(new BigDecimal("1800"), result.get(0).totalCollectedAmount());
    }
}
