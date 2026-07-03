package com.spiceflow.backend.sales.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.service.ProductService;
import com.spiceflow.backend.sales.dto.request.CreateDeliveryRequest;
import com.spiceflow.backend.sales.dto.request.DeliveryPaymentRequest;
import com.spiceflow.backend.sales.dto.request.DeliveryShopItemRequest;
import com.spiceflow.backend.sales.dto.request.DeliveryShopReturnRequest;
import com.spiceflow.backend.sales.dto.request.RecordShopDeliveryRequest;
import com.spiceflow.backend.sales.dto.response.DeliveryResponse;
import com.spiceflow.backend.sales.dto.response.DeliveryShopResponse;
import com.spiceflow.backend.sales.entity.Delivery;
import com.spiceflow.backend.sales.entity.DeliveryShop;
import com.spiceflow.backend.sales.entity.LoadingSheet;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.mapper.DeliveryMapper;
import com.spiceflow.backend.sales.repository.DeliveryRepository;
import com.spiceflow.backend.sales.repository.DeliveryShopRepository;
import com.spiceflow.backend.sales.repository.LoadingSheetRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
import java.math.BigDecimal;
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
class DeliveryServiceTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private DeliveryShopRepository deliveryShopRepository;
    @Mock private LoadingSheetRepository loadingSheetRepository;
    @Mock private ShopRepository shopRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private ProductService productService;
    @Mock private DeliveryMapper deliveryMapper;

    @InjectMocks private DeliveryService deliveryService;

    private Tenant tenant;
    private LoadingSheet loadingSheet;
    private Delivery delivery;
    private Shop shop;
    private Product product;
    @Mock private DeliveryResponse deliveryResponse;
    @Mock private DeliveryShopResponse deliveryShopResponse;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        loadingSheet = new LoadingSheet();
        loadingSheet.setId(1L);
        loadingSheet.setTenant(tenant);
        loadingSheet.setStatus("CONFIRMED");

        delivery = new Delivery();
        delivery.setId(1L);
        delivery.setTenant(tenant);
        delivery.setLoadingSheet(loadingSheet);
        delivery.setStatus("IN_PROGRESS");
        delivery.setShops(new ArrayList<>());

        shop = new Shop();
        shop.setId(1L);
        shop.setTenant(tenant);
        shop.setOutstandingLoan(BigDecimal.ZERO);

        product = new Product();
        product.setId(1L);
        product.setTenant(tenant);
    }

    @Test
    void createDelivery_Success() {
        CreateDeliveryRequest request = CreateDeliveryRequest.builder().loadingSheetId(1L).deliveryDate(java.time.LocalDate.now()).build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(loadingSheetRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(loadingSheet));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);
        when(deliveryMapper.toResponse(delivery)).thenReturn(deliveryResponse);

        DeliveryResponse result = deliveryService.createDelivery(1L, request);

        assertNotNull(result);
    }

    @Test
    void createDelivery_LoadingSheetNotConfirmed() {
        loadingSheet.setStatus("DRAFT");
        CreateDeliveryRequest request = CreateDeliveryRequest.builder().loadingSheetId(1L).deliveryDate(java.time.LocalDate.now()).build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(loadingSheetRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(loadingSheet));

        assertThrows(BusinessRuleViolationException.class, () -> deliveryService.createDelivery(1L, request));
    }

    @Test
    void recordShopDelivery_Success() {
        DeliveryShopItemRequest itemReq = DeliveryShopItemRequest.builder().productId(1L).quantityDelivered(10).rate(java.math.BigDecimal.valueOf(100)).build();
        DeliveryShopReturnRequest returnReq = DeliveryShopReturnRequest.builder().productId(1L).creditValue(java.math.BigDecimal.valueOf(50)).build();
        DeliveryPaymentRequest paymentReq = DeliveryPaymentRequest.builder().amount(java.math.BigDecimal.valueOf(900)).build();
        RecordShopDeliveryRequest request = RecordShopDeliveryRequest.builder().items(java.util.List.of(itemReq)).returns(java.util.List.of(returnReq)).payments(java.util.List.of(paymentReq)).build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(deliveryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(delivery));
        when(shopRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(shop));
        when(deliveryShopRepository.findByDeliveryIdAndShopIdAndTenantId(1L, 1L, 1L)).thenReturn(Optional.empty());
        when(productService.getProductEntity(1L, 1L)).thenReturn(product);

        DeliveryShop deliveryShop = new DeliveryShop();
        when(deliveryShopRepository.save(any(DeliveryShop.class))).thenReturn(deliveryShop);
        when(deliveryMapper.toShopResponse(any())).thenReturn(deliveryShopResponse);

        DeliveryShopResponse result = deliveryService.recordShopDelivery(1L, 1L, 1L, request);

        assertNotNull(result);
        verify(deliveryShopRepository).save(any(DeliveryShop.class));
        verify(shopRepository).save(any(Shop.class)); // Because of credit (1000 - 50 = 950. Paid 900 -> 50 credit)
    }

    @Test
    void completeDelivery_Success() {
        DeliveryShop ds = new DeliveryShop();
        ds.setGrossBillAmount(new BigDecimal("1000"));
        ds.setTotalDiscount(new BigDecimal("100"));
        ds.setReturnsDeducted(new BigDecimal("50"));
        ds.setPaidAmount(new BigDecimal("850"));
        delivery.getShops().add(ds);

        when(deliveryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);
        when(deliveryMapper.toResponse(delivery)).thenReturn(deliveryResponse);

        DeliveryResponse result = deliveryService.completeDelivery(1L, 1L);

        assertNotNull(result);
        assertEquals("COMPLETED", delivery.getStatus());
        assertEquals(new BigDecimal("900"), delivery.getTotalSalesValue());
    }

    @Test
    void getDeliveries_Success() {
        Page<Delivery> page = new PageImpl<>(List.of(delivery));
        when(deliveryRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(deliveryMapper.toResponse(delivery)).thenReturn(deliveryResponse);

        Page<DeliveryResponse> result = deliveryService.getDeliveries(1L, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getDelivery_Success() {
        when(deliveryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(delivery));
        when(deliveryMapper.toResponse(delivery)).thenReturn(deliveryResponse);

        DeliveryResponse result = deliveryService.getDelivery(1L, 1L);
        assertNotNull(result);
    }
}
