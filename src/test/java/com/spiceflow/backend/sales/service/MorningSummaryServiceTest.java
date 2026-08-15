package com.spiceflow.backend.sales.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.repository.ProductRepository;
import com.spiceflow.backend.sales.dto.request.MorningSummaryRequest;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.sales.repository.MorningSummaryRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MorningSummaryServiceTest {

    @Mock private MorningSummaryRepository morningSummaryRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private RepRepository repRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private MorningSummaryService morningSummaryService;

    private Tenant tenant;
    private Rep rep;
    private Driver driver;
    private Product product;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        rep = new Rep();
        rep.setId(1L);

        driver = new Driver();
        driver.setId(1L);

        product = new Product();
        product.setId(1L);
    }

    @Test
    void createMorningSummary_DuplicateProducts_ThrowsException() {
        MorningSummaryRequest.MorningSummaryItemRequest item1 = new MorningSummaryRequest.MorningSummaryItemRequest(1L, 10, null, null);
        MorningSummaryRequest.MorningSummaryItemRequest item2 = new MorningSummaryRequest.MorningSummaryItemRequest(1L, 5, null, null);

        MorningSummaryRequest request = new MorningSummaryRequest(1L, 1L, java.time.LocalDate.now(), List.of(item1, item2), null);

        assertThrows(BusinessRuleViolationException.class, () -> morningSummaryService.createMorningSummary(1L, request));
    }

    @Test
    void updateMorningSummary_DuplicateProducts_ThrowsException() {
        MorningSummaryRequest.MorningSummaryItemRequest item1 = new MorningSummaryRequest.MorningSummaryItemRequest(1L, 10, null, null);
        MorningSummaryRequest.MorningSummaryItemRequest item2 = new MorningSummaryRequest.MorningSummaryItemRequest(1L, 5, null, null);

        MorningSummaryRequest request = new MorningSummaryRequest(1L, 1L, java.time.LocalDate.now(), List.of(item1, item2), null);

        assertThrows(BusinessRuleViolationException.class, () -> morningSummaryService.updateMorningSummary(1L, 1L, request));
    }
}
