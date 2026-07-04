package com.spiceflow.backend.dashboard.logistics.repository;

import com.spiceflow.backend.dashboard.logistics.dto.ActiveLoadingSheetDto;
import com.spiceflow.backend.dashboard.logistics.dto.InProgressDeliveryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class LogisticsDashboardRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private LogisticsDashboardRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LogisticsDashboardRepository(jdbcTemplate);
    }

    @Test
    void should_be_instantiable_with_jdbc_template() {
        assertNotNull(repository);
    }

    @Test
    void summary_metrics_record_holds_all_fields() {
        var metrics = new LogisticsDashboardRepository.SummaryMetrics(4L, 2L, 8L, 12L);
        assertEquals(4L, metrics.activeLoadingSheetsCount());
        assertEquals(2L, metrics.inProgressDeliveriesCount());
        assertEquals(8L, metrics.completedDeliveriesToday());
        assertEquals(12L, metrics.totalReturnItemsToday());
    }

    @Test
    void active_loading_sheet_dto_holds_all_fields() {
        ActiveLoadingSheetDto dto = new ActiveLoadingSheetDto(1L, "LS-001", 10L, "John Driver", "LOADED", "2026-07-04", 45);
        assertEquals(1L, dto.id());
        assertEquals("LS-001", dto.sheetNumber());
        assertEquals("John Driver", dto.driverName());
        assertEquals("LOADED", dto.status());
        assertEquals(45, dto.itemCount());
    }

    @Test
    void in_progress_delivery_dto_holds_all_fields() {
        InProgressDeliveryDto dto = new InProgressDeliveryDto(5L, "DEL-005", "LS-001", "John Driver", "IN_PROGRESS", "2026-07-04", 8);
        assertEquals(5L, dto.id());
        assertEquals("DEL-005", dto.deliveryNumber());
        assertEquals("LS-001", dto.loadingSheetNumber());
        assertEquals("IN_PROGRESS", dto.status());
        assertEquals(8, dto.shopCount());
    }
}
