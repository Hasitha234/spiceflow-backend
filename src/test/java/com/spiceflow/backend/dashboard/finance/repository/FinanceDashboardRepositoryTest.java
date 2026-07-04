package com.spiceflow.backend.dashboard.finance.repository;

import com.spiceflow.backend.dashboard.finance.dto.ReceivableAgingBucketDto;
import com.spiceflow.backend.dashboard.finance.dto.RecentFinancialTransactionDto;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class FinanceDashboardRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private FinanceDashboardRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FinanceDashboardRepository(jdbcTemplate);
    }

    @Test
    void should_be_instantiable_with_jdbc_template() {
        assertNotNull(repository);
    }

    @Test
    void summary_metrics_record_holds_all_fields() {
        var metrics = new FinanceDashboardRepository.SummaryMetrics(
            new BigDecimal("150000.00"),
            new BigDecimal("80000.00"),
            new BigDecimal("45000.00"),
            new BigDecimal("20000.00")
        );
        assertEquals(new BigDecimal("150000.00"), metrics.totalReceivables());
        assertEquals(new BigDecimal("80000.00"), metrics.totalPayables());
        assertEquals(new BigDecimal("45000.00"), metrics.monthCollections());
        assertEquals(new BigDecimal("20000.00"), metrics.monthPoSpent());
    }

    @Test
    void aging_bucket_dto_holds_all_fields() {
        ReceivableAgingBucketDto dto = new ReceivableAgingBucketDto("0–30 Days", 15L, new BigDecimal("50000.00"));
        assertEquals("0–30 Days", dto.bucketLabel());
        assertEquals(15L, dto.shopCount());
        assertEquals(new BigDecimal("50000.00"), dto.totalAmount());
    }

    @Test
    void recent_tx_dto_holds_all_fields() {
        Instant now = Instant.now();
        RecentFinancialTransactionDto dto = new RecentFinancialTransactionDto(1L, "COLLECTION", "CC-001", "Shop A", new BigDecimal("1000.00"), "CASH", now, "CONFIRMED");
        assertEquals(1L, dto.id());
        assertEquals("COLLECTION", dto.transactionType());
        assertEquals("Shop A", dto.partyName());
        assertEquals("CASH", dto.paymentMethod());
        assertEquals("CONFIRMED", dto.status());
    }
}
