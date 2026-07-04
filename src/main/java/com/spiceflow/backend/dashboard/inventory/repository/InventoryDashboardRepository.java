package com.spiceflow.backend.dashboard.inventory.repository;

import com.spiceflow.backend.dashboard.inventory.dto.LowStockItemDto;
import com.spiceflow.backend.dashboard.inventory.dto.RecentMovementDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Pure CQRS Read Model repository for the Inventory Dashboard.
 * Executes read-only SQL aggregations against inventory_items, products, warehouse_transfers,
 * and inventory_ledger_entries without JPA entity dependencies.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InventoryDashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public record SummaryMetrics(
        BigDecimal totalStockValue,
        long totalItemsCount,
        long lowStockCount,
        long pendingTransfersCount
    ) {}

    public SummaryMetrics getSummaryMetrics(Long tenantId) {
        String sql = """
            SELECT
                (SELECT COALESCE(SUM(i.quantity_available * COALESCE(p.base_price, 0)), 0.00)
                 FROM inventory_items i
                 JOIN products p ON i.product_id = p.id
                 WHERE i.tenant_id = :tenantId AND i.deleted_at IS NULL AND p.deleted_at IS NULL) AS total_val,
                (SELECT COUNT(*)
                 FROM products
                 WHERE tenant_id = :tenantId AND deleted_at IS NULL) AS total_items,
                (SELECT COUNT(*)
                 FROM (
                     SELECT p.id
                     FROM products p
                     LEFT JOIN inventory_items i ON p.id = i.product_id AND i.deleted_at IS NULL
                     WHERE p.tenant_id = :tenantId AND p.deleted_at IS NULL
                     GROUP BY p.id
                     HAVING COALESCE(SUM(i.quantity_available), 0) < 10
                 ) sub) AS low_stock,
                (SELECT COUNT(*)
                 FROM warehouse_transfers
                 WHERE tenant_id = :tenantId AND status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'IN_TRANSIT')) AS pending_transfers
            """;
        MapSqlParameterSource params = new MapSqlParameterSource("tenantId", tenantId);

        SummaryMetrics metrics = jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> new SummaryMetrics(
            rs.getBigDecimal("total_val") != null ? rs.getBigDecimal("total_val") : BigDecimal.ZERO,
            rs.getLong("total_items"),
            rs.getLong("low_stock"),
            rs.getLong("pending_transfers")
        ));
        return metrics != null ? metrics : new SummaryMetrics(BigDecimal.ZERO, 0L, 0L, 0L);
    }

    public List<LowStockItemDto> getLowStockItems(Long tenantId, int limit) {
        String sql = """
            SELECT
                p.id AS product_id,
                p.sku,
                p.name,
                COALESCE(SUM(i.quantity_available), 0) AS total_qty,
                COALESCE(p.unit_of_measure, 'PCS') AS uom,
                COALESCE(p.base_price, 0.00) AS base_price
            FROM products p
            LEFT JOIN inventory_items i ON p.id = i.product_id AND i.deleted_at IS NULL
            WHERE p.tenant_id = :tenantId AND p.deleted_at IS NULL
            GROUP BY p.id, p.sku, p.name, p.unit_of_measure, p.base_price
            HAVING COALESCE(SUM(i.quantity_available), 0) < 10
            ORDER BY total_qty ASC
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("limit", limit);

        List<LowStockItemDto> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> new LowStockItemDto(
            rs.getLong("product_id"),
            rs.getString("sku") != null ? rs.getString("sku") : "",
            rs.getString("name") != null ? rs.getString("name") : "",
            rs.getInt("total_qty"),
            rs.getString("uom") != null ? rs.getString("uom") : "PCS",
            rs.getBigDecimal("base_price") != null ? rs.getBigDecimal("base_price") : BigDecimal.ZERO
        ));
        return results != null ? results : Collections.emptyList();
    }

    public List<RecentMovementDto> getRecentMovements(Long tenantId, int limit) {
        String sql = """
            SELECT
                l.id,
                l.movement_type,
                l.product_id,
                COALESCE(p.name, 'Unknown Product') AS product_name,
                l.quantity,
                l.total_value,
                l.reference_id,
                l.timestamp,
                l.performed_by
            FROM inventory_ledger_entries l
            LEFT JOIN products p ON l.product_id = p.id
            WHERE l.tenant_id = :tenantId
            ORDER BY l.timestamp DESC
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("limit", limit);

        List<RecentMovementDto> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Instant ts = rs.getTimestamp("timestamp") != null ? rs.getTimestamp("timestamp").toInstant() : Instant.EPOCH;
            return new RecentMovementDto(
                rs.getLong("id"),
                rs.getString("movement_type") != null ? rs.getString("movement_type") : "",
                rs.getLong("product_id"),
                rs.getString("product_name") != null ? rs.getString("product_name") : "",
                rs.getBigDecimal("quantity") != null ? rs.getBigDecimal("quantity") : BigDecimal.ZERO,
                rs.getBigDecimal("total_value") != null ? rs.getBigDecimal("total_value") : BigDecimal.ZERO,
                rs.getString("reference_id") != null ? rs.getString("reference_id") : "",
                ts,
                rs.getString("performed_by") != null ? rs.getString("performed_by") : ""
            );
        });
        return results != null ? results : Collections.emptyList();
    }
}
