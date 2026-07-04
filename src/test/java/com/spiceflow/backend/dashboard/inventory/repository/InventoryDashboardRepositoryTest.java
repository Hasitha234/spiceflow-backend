package com.spiceflow.backend.dashboard.inventory.repository;

import com.spiceflow.backend.dashboard.inventory.dto.InventoryDashboardResponse;
import com.spiceflow.backend.dashboard.inventory.dto.LowStockItemDto;
import com.spiceflow.backend.dashboard.inventory.dto.RecentMovementDto;
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

@ExtendWith(MockitoExtension.class)
class InventoryDashboardRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private InventoryDashboardRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InventoryDashboardRepository(jdbcTemplate);
    }

    @Test
    void should_be_instantiable_with_jdbc_template() {
        assertNotNull(repository);
    }

    @Test
    void summary_metrics_record_holds_all_fields() {
        var metrics = new InventoryDashboardRepository.SummaryMetrics(
            new BigDecimal("50000.00"),
            100L,
            5L,
            2L
        );
        assertEquals(new BigDecimal("50000.00"), metrics.totalStockValue());
        assertEquals(100L, metrics.totalItemsCount());
        assertEquals(5L, metrics.lowStockCount());
        assertEquals(2L, metrics.pendingTransfersCount());
    }

    @Test
    void low_stock_item_dto_holds_all_fields() {
        LowStockItemDto dto = new LowStockItemDto(1L, "SKU-001", "Cinnamon", 5, "KG", new BigDecimal("12.50"));
        assertEquals(1L, dto.productId());
        assertEquals("SKU-001", dto.sku());
        assertEquals("Cinnamon", dto.name());
        assertEquals(5, dto.quantityAvailable());
        assertEquals("KG", dto.unitOfMeasure());
        assertEquals(new BigDecimal("12.50"), dto.basePrice());
    }

    @Test
    void recent_movement_dto_holds_all_fields() {
        Instant now = Instant.now();
        RecentMovementDto dto = new RecentMovementDto(10L, "RECEIPT", 1L, "Cinnamon", new BigDecimal("100.00"), new BigDecimal("1250.00"), "GR-001", now, "admin");
        assertEquals(10L, dto.id());
        assertEquals("RECEIPT", dto.movementType());
        assertEquals("Cinnamon", dto.productName());
        assertEquals("GR-001", dto.referenceId());
        assertEquals(now, dto.timestamp());
    }
}
