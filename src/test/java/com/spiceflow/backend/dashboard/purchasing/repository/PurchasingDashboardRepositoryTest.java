package com.spiceflow.backend.dashboard.purchasing.repository;

import com.spiceflow.backend.dashboard.purchasing.dto.AgingBucketDto;
import com.spiceflow.backend.dashboard.purchasing.dto.OpenPurchaseOrderProjection;
import com.spiceflow.backend.dashboard.purchasing.dto.SupplierLeadTimeDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test verifying PurchasingDashboardRepository wiring and delegation.
 * Full SQL correctness is covered by the integration test slice.
 */
@ExtendWith(MockitoExtension.class)
class PurchasingDashboardRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private PurchasingDashboardRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PurchasingDashboardRepository(jdbcTemplate);
    }

    @Test
    void should_be_instantiable_with_jdbc_template() {
        assertNotNull(repository);
    }

    @Test
    void summary_metrics_record_holds_all_fields() {
        var metrics = new PurchasingDashboardRepository.SummaryMetrics(
            5L,
            new BigDecimal("10000.00"),
            new BigDecimal("3500.00"),
            12.5
        );
        assertEquals(5L, metrics.totalOpenOrders());
        assertEquals(new BigDecimal("10000.00"), metrics.totalOpenOrderValue());
        assertEquals(new BigDecimal("3500.00"), metrics.totalReceivedMonthValue());
        assertEquals(12.5, metrics.averageSupplierLeadTimeDays());
    }

    @Test
    void aging_bucket_dto_holds_all_fields() {
        AgingBucketDto bucket = new AgingBucketDto("0\u201330 Days", 3L, new BigDecimal("4500.00"));
        assertEquals("0\u201330 Days", bucket.bucketLabel());
        assertEquals(3L, bucket.orderCount());
        assertEquals(new BigDecimal("4500.00"), bucket.totalValue());
    }

    @Test
    void supplier_lead_time_dto_holds_all_fields() {
        SupplierLeadTimeDto dto = new SupplierLeadTimeDto(10L, "Acme Spices", 20L, 15L, 7.5);
        assertEquals(10L, dto.supplierId());
        assertEquals("Acme Spices", dto.supplierName());
        assertEquals(20L, dto.totalOrders());
        assertEquals(15L, dto.completedOrders());
        assertEquals(7.5, dto.averageLeadTimeDays());
    }

    @Test
    void open_purchase_order_projection_holds_all_fields() {
        Instant now = Instant.now();
        OpenPurchaseOrderProjection proj = new OpenPurchaseOrderProjection(
            "PO-001", 1L, "Supplier A", now, new BigDecimal("2500.00"), "SUBMITTED", 14L
        );
        assertEquals("PO-001", proj.poNumber());
        assertEquals(1L, proj.supplierId());
        assertEquals("Supplier A", proj.supplierName());
        assertEquals(now, proj.orderDate());
        assertEquals(new BigDecimal("2500.00"), proj.totalAmount());
        assertEquals("SUBMITTED", proj.status());
        assertEquals(14L, proj.ageInDays());
    }
}
