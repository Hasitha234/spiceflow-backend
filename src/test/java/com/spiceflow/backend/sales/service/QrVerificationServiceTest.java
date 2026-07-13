package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.sales.dto.QrVerificationDtos.*;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.entity.ShopVisit;
import com.spiceflow.backend.sales.repository.DeliveryRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
import com.spiceflow.backend.sales.repository.ShopVisitRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrVerificationServiceTest {

    @Mock
    private ShopRepository shopRepository;
    @Mock
    private ShopVisitRepository shopVisitRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private QrVerificationService qrVerificationService;

    private Shop shop;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        shop = new Shop();
        shop.setId(10L);
        shop.setName("Test Shop");
        shop.setTenant(tenant);
    }

    @Test
    @DisplayName("getShopQrData returns valid response when shop exists")
    void getShopQrData_success() {
        when(shopRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(shop));

        ShopQrResponse response = qrVerificationService.getShopQrData(10L, 1L);

        assertThat(response.shopId()).isEqualTo(10L);
        assertThat(response.shopName()).isEqualTo("Test Shop");
        assertThat(response.tenantId()).isEqualTo(1L);
        assertThat(response.qrPayload()).isEqualTo("SPICEFLOW:SHOP:1:10");
    }

    @Test
    @DisplayName("getShopQrData throws exception when shop not found")
    void getShopQrData_notFound() {
        when(shopRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qrVerificationService.getShopQrData(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Shop not found");
    }

    @Test
    @DisplayName("verifyVisit verifies visit when request is valid without delivery")
    void verifyVisit_successWithoutDelivery() {
        QrVerifyRequest request = new QrVerifyRequest(10L, null, 6.9, 79.8, "Visited");

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(shopRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(shop));

        ShopVisit savedVisit = ShopVisit.builder()
                .tenant(tenant)
                .shop(shop)
                .visitedAt(OffsetDateTime.now())
                .verified(true)
                .notes("Visited")
                .build();
        savedVisit.setId(100L);

        when(shopVisitRepository.save(any(ShopVisit.class))).thenReturn(savedVisit);

        ShopVisitResponse response = qrVerificationService.verifyVisit(request, 1L);

        assertThat(response.visitId()).isEqualTo(100L);
        assertThat(response.shopId()).isEqualTo(10L);
        assertThat(response.shopName()).isEqualTo("Test Shop");
        assertThat(response.verified()).isTrue();
    }

    @Test
    @DisplayName("verifyVisit throws exception when tenant not found")
    void verifyVisit_tenantNotFound() {
        QrVerifyRequest request = new QrVerifyRequest(10L, null, null, null, null);
        when(tenantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qrVerificationService.verifyVisit(request, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tenant not found");
    }

    @Test
    @DisplayName("getDeliveryVisits returns list of visit responses")
    void getDeliveryVisits_success() {
        ShopVisit visit = ShopVisit.builder()
                .tenant(tenant)
                .shop(shop)
                .visitedAt(OffsetDateTime.now())
                .verified(true)
                .build();
        visit.setId(100L);

        when(shopVisitRepository.findByDeliveryIdAndTenantId(50L, 1L)).thenReturn(List.of(visit));

        List<ShopVisitResponse> responses = qrVerificationService.getDeliveryVisits(50L, 1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).visitId()).isEqualTo(100L);
        assertThat(responses.get(0).shopId()).isEqualTo(10L);
    }
}
