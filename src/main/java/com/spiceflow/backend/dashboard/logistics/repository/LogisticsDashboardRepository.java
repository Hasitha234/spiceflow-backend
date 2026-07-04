package com.spiceflow.backend.dashboard.logistics.repository;

import com.spiceflow.backend.dashboard.logistics.dto.ActiveLoadingSheetDto;
import com.spiceflow.backend.dashboard.logistics.dto.InProgressDeliveryDto;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Pure CQRS Read Model repository for the Logistics Dashboard.
 * Executes read-only SQL aggregations against loading_sheets, deliveries, drivers, and delivery_shop_returns.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LogisticsDashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public record SummaryMetrics(
        long activeLoadingSheetsCount,
        long inProgressDeliveriesCount,
        long completedDeliveriesToday,
        long totalReturnItemsToday
    ) {}

    public SummaryMetrics getSummaryMetrics(Long tenantId) {
        String sql = """
            SELECT
                (SELECT COUNT(*)
                 FROM loading_sheets
                 WHERE tenant_id = :tenantId AND deleted_at IS NULL AND status IN ('DRAFT', 'PENDING', 'LOADED', 'DISPATCHED')) AS active_ls,
                (SELECT COUNT(*)
                 FROM deliveries
                 WHERE tenant_id = :tenantId AND deleted_at IS NULL AND status IN ('IN_PROGRESS', 'DISPATCHED')) AS active_del,
                (SELECT COUNT(*)
                 FROM deliveries
                 WHERE tenant_id = :tenantId AND deleted_at IS NULL AND status = 'COMPLETED' AND delivery_date = CURRENT_DATE) AS completed_today,
                (SELECT COALESCE(SUM(r.quantity_returned), 0)
                 FROM delivery_shop_returns r
                 JOIN delivery_shops ds ON r.delivery_shop_id = ds.id
                 JOIN deliveries d ON ds.delivery_id = d.id
                 WHERE r.tenant_id = :tenantId AND d.delivery_date = CURRENT_DATE) AS returns_today
            """;
        MapSqlParameterSource params = new MapSqlParameterSource("tenantId", tenantId);

        SummaryMetrics metrics = jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> new SummaryMetrics(
            rs.getLong("active_ls"),
            rs.getLong("active_del"),
            rs.getLong("completed_today"),
            rs.getLong("returns_today")
        ));
        return metrics != null ? metrics : new SummaryMetrics(0L, 0L, 0L, 0L);
    }

    public List<ActiveLoadingSheetDto> getActiveLoadingSheets(Long tenantId, int limit) {
        String sql = """
            SELECT
                ls.id,
                COALESCE(ls.sheet_number, CONCAT('LS-', ls.id)) AS sheet_no,
                ls.driver_id,
                COALESCE(d.name, 'Unknown Driver') AS driver_name,
                ls.status,
                CAST(ls.loading_date AS VARCHAR) AS loading_date,
                (SELECT COALESCE(SUM(i.quantity_loaded), 0) FROM loading_sheet_items i WHERE i.loading_sheet_id = ls.id) AS item_count
            FROM loading_sheets ls
            LEFT JOIN drivers d ON ls.driver_id = d.id
            WHERE ls.tenant_id = :tenantId AND ls.deleted_at IS NULL AND ls.status IN ('DRAFT', 'PENDING', 'LOADED', 'DISPATCHED')
            ORDER BY ls.created_at DESC
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("limit", limit);

        List<ActiveLoadingSheetDto> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> new ActiveLoadingSheetDto(
            rs.getLong("id"),
            rs.getString("sheet_no") != null ? rs.getString("sheet_no") : "",
            rs.getLong("driver_id"),
            rs.getString("driver_name") != null ? rs.getString("driver_name") : "",
            rs.getString("status") != null ? rs.getString("status") : "",
            rs.getString("loading_date") != null ? rs.getString("loading_date") : "",
            rs.getInt("item_count")
        ));
        return results != null ? results : Collections.emptyList();
    }

    public List<InProgressDeliveryDto> getInProgressDeliveries(Long tenantId, int limit) {
        String sql = """
            SELECT
                del.id,
                COALESCE(del.delivery_number, CONCAT('DEL-', del.id)) AS del_no,
                COALESCE(ls.sheet_number, CONCAT('LS-', del.loading_sheet_id)) AS ls_no,
                COALESCE(d.name, 'Unknown Driver') AS driver_name,
                del.status,
                CAST(del.delivery_date AS VARCHAR) AS del_date,
                (SELECT COUNT(*) FROM delivery_shops ds WHERE ds.delivery_id = del.id) AS shop_count
            FROM deliveries del
            LEFT JOIN loading_sheets ls ON del.loading_sheet_id = ls.id
            LEFT JOIN drivers d ON ls.driver_id = d.id
            WHERE del.tenant_id = :tenantId AND del.deleted_at IS NULL AND del.status IN ('IN_PROGRESS', 'DISPATCHED')
            ORDER BY del.created_at DESC
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("limit", limit);

        List<InProgressDeliveryDto> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> new InProgressDeliveryDto(
            rs.getLong("id"),
            rs.getString("del_no") != null ? rs.getString("del_no") : "",
            rs.getString("ls_no") != null ? rs.getString("ls_no") : "",
            rs.getString("driver_name") != null ? rs.getString("driver_name") : "",
            rs.getString("status") != null ? rs.getString("status") : "",
            rs.getString("del_date") != null ? rs.getString("del_date") : "",
            rs.getInt("shop_count")
        ));
        return results != null ? results : Collections.emptyList();
    }
}
