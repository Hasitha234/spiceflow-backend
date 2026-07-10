package com.spiceflow.backend.dashboard.sales.repository;

import com.spiceflow.backend.dashboard.sales.dto.RecentRepOrderDto;
import com.spiceflow.backend.dashboard.sales.dto.TopDebtorShopDto;
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
 * Pure CQRS Read Model repository for the Sales Dashboard.
 * Executes read-only SQL aggregations against rep_orders, cash_collections, shops, and reps.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SalesDashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public record SummaryMetrics(
        BigDecimal todaySalesValue,
        BigDecimal monthSalesValue,
        BigDecimal monthCollectionsValue,
        BigDecimal totalOutstandingLoans
    ) {}

    public SummaryMetrics getSummaryMetrics(Long tenantId, Instant startOfMonth) {
        String sql = """
            SELECT
                (SELECT COALESCE(SUM(net_amount), 0.00)
                 FROM rep_orders
                 WHERE tenant_id = :tenantId AND deleted_at IS NULL AND status IN ('SUBMITTED', 'APPROVED', 'LOADED', 'DELIVERED') AND CAST(order_date AS DATE) = CURRENT_DATE) AS today_sales,
                (SELECT COALESCE(SUM(net_amount), 0.00)
                 FROM rep_orders
                 WHERE tenant_id = :tenantId AND deleted_at IS NULL AND status IN ('SUBMITTED', 'APPROVED', 'LOADED', 'DELIVERED') AND created_at >= :startOfMonth) AS month_sales,
                (SELECT COALESCE(SUM(amount), 0.00)
                 FROM cash_collections
                 WHERE tenant_id = :tenantId AND status = 'CONFIRMED' AND confirmed_at >= :startOfMonth) AS month_collections,
                (SELECT COALESCE(SUM(outstanding_loan), 0.00)
                 FROM shops
                 WHERE tenant_id = :tenantId AND deleted_at IS NULL) AS total_loans
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("startOfMonth", java.sql.Timestamp.from(startOfMonth));

        SummaryMetrics metrics = jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> new SummaryMetrics(
            rs.getBigDecimal("today_sales") != null ? rs.getBigDecimal("today_sales") : BigDecimal.ZERO,
            rs.getBigDecimal("month_sales") != null ? rs.getBigDecimal("month_sales") : BigDecimal.ZERO,
            rs.getBigDecimal("month_collections") != null ? rs.getBigDecimal("month_collections") : BigDecimal.ZERO,
            rs.getBigDecimal("total_loans") != null ? rs.getBigDecimal("total_loans") : BigDecimal.ZERO
        ));
        return metrics != null ? metrics : new SummaryMetrics(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public List<RecentRepOrderDto> getRecentOrders(Long tenantId, int limit) {
        String sql = """
            SELECT
                ro.id,
                COALESCE(ro.order_number, CONCAT('RO-', ro.id)) AS order_no,
                ro.rep_id,
                COALESCE(r.name, 'Unknown Rep') AS rep_name,
                ro.status,
                CAST(ro.order_date AS VARCHAR) AS order_date,
                COALESCE(ro.net_amount, 0.00) AS total_amt,
                (SELECT COUNT(*) FROM rep_order_shops ros WHERE ros.rep_order_id = ro.id) AS shop_count
            FROM rep_orders ro
            LEFT JOIN reps r ON ro.rep_id = r.id
            WHERE ro.tenant_id = :tenantId AND ro.deleted_at IS NULL
            ORDER BY ro.created_at DESC
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("limit", limit);

        List<RecentRepOrderDto> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> new RecentRepOrderDto(
            rs.getLong("id"),
            rs.getString("order_no") != null ? rs.getString("order_no") : "",
            rs.getLong("rep_id"),
            rs.getString("rep_name") != null ? rs.getString("rep_name") : "",
            rs.getString("status") != null ? rs.getString("status") : "",
            rs.getString("order_date") != null ? rs.getString("order_date") : "",
            rs.getBigDecimal("total_amt") != null ? rs.getBigDecimal("total_amt") : BigDecimal.ZERO,
            rs.getInt("shop_count")
        ));
        return results != null ? results : Collections.emptyList();
    }

    public List<TopDebtorShopDto> getTopDebtorShops(Long tenantId, int limit) {
        String sql = """
            SELECT
                id AS shop_id,
                COALESCE(name, 'Unknown Shop') AS shop_name,
                COALESCE(owner_name, '') AS owner_name,
                COALESCE(phone, '') AS phone,
                COALESCE(area, '') AS area,
                COALESCE(outstanding_loan, 0.00) AS loan
            FROM shops
            WHERE tenant_id = :tenantId AND deleted_at IS NULL AND outstanding_loan > 0
            ORDER BY outstanding_loan DESC
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("limit", limit);

        List<TopDebtorShopDto> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> new TopDebtorShopDto(
            rs.getLong("shop_id"),
            rs.getString("shop_name") != null ? rs.getString("shop_name") : "",
            rs.getString("owner_name") != null ? rs.getString("owner_name") : "",
            rs.getString("phone") != null ? rs.getString("phone") : "",
            rs.getString("area") != null ? rs.getString("area") : "",
            rs.getBigDecimal("loan") != null ? rs.getBigDecimal("loan") : BigDecimal.ZERO
        ));
        return results != null ? results : Collections.emptyList();
    }
}
