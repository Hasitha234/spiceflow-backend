package com.spiceflow.backend.sales.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.service.ProductService;
import com.spiceflow.backend.sales.dto.request.CreateRepOrderRequest;
import com.spiceflow.backend.sales.dto.request.RepOrderItemRequest;
import com.spiceflow.backend.sales.dto.request.RepOrderShopRequest;
import com.spiceflow.backend.sales.dto.request.ShopReturnRequest;
import com.spiceflow.backend.sales.dto.response.RepOrderResponse;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.mapper.RepOrderMapper;
import com.spiceflow.backend.sales.repository.RepOrderRepository;
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
class RepOrderServiceTest {

    @Mock private RepOrderRepository repOrderRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private SalesMasterDataService salesMasterDataService;
    @Mock private ProductService productService;
    @Mock private RepOrderMapper repOrderMapper;

    @InjectMocks private RepOrderService repOrderService;

    private Tenant tenant;
    private Rep rep;
    private Shop shop;
    private Product product;
    private RepOrder repOrder;
    @Mock private RepOrderResponse response;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        rep = new Rep();
        rep.setId(1L);

        shop = new Shop();
        shop.setId(1L);

        product = new Product();
        product.setId(1L);

        repOrder = new RepOrder();
        repOrder.setId(1L);
        repOrder.setTenant(tenant);
    }

    @Test
    void createRepOrder_Success() {
        CreateRepOrderRequest request = new CreateRepOrderRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "repId", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(request, "orderDate", LocalDate.now());
        
        RepOrderItemRequest itemReq = new RepOrderItemRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(itemReq, "productId", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(itemReq, "quantity", 10);
        org.springframework.test.util.ReflectionTestUtils.setField(itemReq, "rate", new BigDecimal("10.00"));
        
        ShopReturnRequest returnReq = new ShopReturnRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(returnReq, "productId", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(returnReq, "quantity", 2);
        org.springframework.test.util.ReflectionTestUtils.setField(returnReq, "creditValue", new BigDecimal("10.00"));

        RepOrderShopRequest shopReq = new RepOrderShopRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(shopReq, "shopId", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(shopReq, "items", List.of(itemReq));
        org.springframework.test.util.ReflectionTestUtils.setField(shopReq, "returns", List.of(returnReq));

        org.springframework.test.util.ReflectionTestUtils.setField(request, "shops", List.of(shopReq));

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(salesMasterDataService.getRepEntity(1L, 1L)).thenReturn(rep);
        when(salesMasterDataService.getShopEntity(1L, 1L)).thenReturn(shop);
        when(productService.getProductEntity(1L, 1L)).thenReturn(product);
        when(repOrderRepository.save(any(RepOrder.class))).thenReturn(repOrder);
        when(repOrderMapper.toResponse(repOrder)).thenReturn(response);

        RepOrderResponse result = repOrderService.createRepOrder(1L, request);

        assertNotNull(result);
        verify(repOrderRepository).save(any(RepOrder.class));
    }

    @Test
    void getRepOrders_WithoutRepId() {
        Page<RepOrder> page = new PageImpl<>(List.of(repOrder));
        when(repOrderRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(repOrderMapper.toResponse(repOrder)).thenReturn(response);

        Page<RepOrderResponse> result = repOrderService.getRepOrders(1L, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getRepOrders_WithRepId() {
        Page<RepOrder> page = new PageImpl<>(List.of(repOrder));
        when(repOrderRepository.findByTenantIdAndRepId(eq(1L), eq(1L), any(PageRequest.class))).thenReturn(page);
        when(repOrderMapper.toResponse(repOrder)).thenReturn(response);

        Page<RepOrderResponse> result = repOrderService.getRepOrders(1L, 1L, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getRepOrder_Success() {
        when(repOrderRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(repOrder));
        when(repOrderMapper.toResponse(repOrder)).thenReturn(response);

        RepOrderResponse result = repOrderService.getRepOrder(1L, 1L);

        assertNotNull(result);
    }
}
