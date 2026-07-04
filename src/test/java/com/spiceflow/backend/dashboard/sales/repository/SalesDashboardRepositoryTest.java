package com.spiceflow.backend.dashboard.sales.repository;

import com.spiceflow.backend.dashboard.sales.dto.RecentRepOrderDto;
import com.spiceflow.backend.dashboard.sales.dto.TopDebtorShopDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SalesDashboardRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SalesDashboardRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SalesDashboardRepository(jdbcTemplate);
    }

    @Test
    void should_be_instantiable_with_jdbc_template() {
        assertNotNull(repository);
    }

    @Test
    void summary_metrics_record_holds_all_fields() {
        var metrics = new SalesDashboardRepository.SummaryMetrics(
            new BigDecimal("1500.00"),
            new BigDecimal("45000.00"),
            new BigDecimal("40000.00"),
            new BigDecimal("125000.00")
        );
        assertEquals(new BigDecimal("1500.00"), metrics.todaySalesValue());
        assertEquals(new BigDecimal("45000.00"), metrics.monthSalesValue());
        assertEquals(new BigDecimal("40000.00"), metrics.monthCollectionsValue());
        assertEquals(new BigDecimal("125000.00"), metrics.totalOutstandingLoans());
    }

    @Test
    void recent_rep_order_dto_holds_all_fields() {
        RecentRepOrderDto dto = new RecentRepOrderDto(1L, "RO-001", 10L, "Alice Rep", "SUBMITTED", "2026-07-04", new BigDecimal("500.00"), 3);
        assertEquals(1L, dto.id());
        assertEquals("RO-001", dto.orderNumber());
        assertEquals("Alice Rep", dto.repName());
        assertEquals(new BigDecimal("500.00"), dto.totalAmount());
        assertEquals(3, dto.shopCount());
    }

    @Test
    void top_debtor_shop_dto_holds_all_fields() {
        TopDebtorShopDto dto = new TopDebtorShopDto(2L, "Spice Market", "Bob", "555-0100", "Downtown", new BigDecimal("5000.00"));
        assertEquals(2L, dto.shopId());
        assertEquals("Spice Market", dto.shopName());
        assertEquals("Downtown", dto.area());
        assertEquals(new BigDecimal("5000.00"), dto.outstandingLoan());
    }
}
