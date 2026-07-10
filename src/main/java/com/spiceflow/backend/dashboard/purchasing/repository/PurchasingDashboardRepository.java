package com.spiceflow.backend.dashboard.purchasing.repository;

import com.spiceflow.backend.dashboard.purchasing.dto.AgingBucketDto;
import com.spiceflow.backend.dashboard.purchasing.dto.OpenPurchaseOrderProjection;
import com.spiceflow.backend.dashboard.purchasing.dto.SupplierLeadTimeDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Pure CQRS Read Model repository for the Purchasing Dashboard.
 * Executes read-only SQL aggregations against purchase_orders and suppliers tables
 * without importing or depending on transactional JPA entities (ADR-013 Point 10).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PurchasingDashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public record SummaryMetrics(
        long totalOpenOrders,
        BigDecimal totalOpenOrderValue,
        BigDecimal totalReceivedMonthValue,
        double averageSupplierLeadTimeDays
    ) {}

    public SummaryMetrics getSummaryMetrics(Long tenantId, Instant startOfMonth) {
        String sql = """
            SELECT
                COALESCE(SUM(CASE WHEN status IN ('SUBMITTED', 'APPROVED', 'ORDERED', 'PARTIALLY_RECEIVED') THEN 1 ELSE 0 END), 0) AS total_open_orders,
                COALESCE(SUM(CASE WHEN status IN ('SUBMITTED', 'APPROVED', 'ORDERED', 'PARTIALLY_RECEIVED') THEN total_amount ELSE 0.00 END), 0.00) AS total_open_val,
                COALESCE(SUM(CASE WHEN status IN ('RECEIVED', 'CLOSED') AND order_date >= :startOfMonth THEN total_amount ELSE 0.00 END), 0.00) AS total_received_val,
                COALESCE(AVG(CASE WHEN status IN ('RECEIVED', 'CLOSED') THEN (CAST(COALESCE(received_at, updated_at) AS DATE) - CAST(COALESCE(submitted_at, order_date) AS DATE)) ELSE NULL END), 0.0) AS avg_lead_time
            FROM purchase_orders
            WHERE tenant_id = :tenantId
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("startOfMonth", java.sql.Timestamp.from(startOfMonth));

        SummaryMetrics metrics = jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> new SummaryMetrics(
            rs.getLong("total_open_orders"),
            rs.getBigDecimal("total_open_val") != null ? rs.getBigDecimal("total_open_val") : BigDecimal.ZERO,
            rs.getBigDecimal("total_received_val") != null ? rs.getBigDecimal("total_received_val") : BigDecimal.ZERO,
            rs.getDouble("avg_lead_time")
        ));
        return metrics != null ? metrics : new SummaryMetrics(0L, BigDecimal.ZERO, BigDecimal.ZERO, 0.0);
    }

    public List<AgingBucketDto> getAgingBuckets(Long tenantId) {
        String sql = """
            SELECT
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(order_date AS DATE)) <= 30 THEN 1 ELSE 0 END), 0) as count_0_30,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(order_date AS DATE)) <= 30 THEN total_amount ELSE 0.00 END), 0.00) as val_0_30,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(order_date AS DATE)) BETWEEN 31 AND 60 THEN 1 ELSE 0 END), 0) as count_31_60,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(order_date AS DATE)) BETWEEN 31 AND 60 THEN total_amount ELSE 0.00 END), 0.00) as val_31_60,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(order_date AS DATE)) BETWEEN 61 AND 90 THEN 1 ELSE 0 END), 0) as count_61_90,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(order_date AS DATE)) BETWEEN 61 AND 90 THEN total_amount ELSE 0.00 END), 0.00) as val_61_90,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(order_date AS DATE)) > 90 THEN 1 ELSE 0 END), 0) as count_90_plus,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(order_date AS DATE)) > 90 THEN total_amount ELSE 0.00 END), 0.00) as val_90_plus
            FROM purchase_orders
            WHERE tenant_id = :tenantId
              AND status IN ('SUBMITTED', 'APPROVED', 'ORDERED', 'PARTIALLY_RECEIVED')
            """;
        MapSqlParameterSource params = new MapSqlParameterSource("tenantId", tenantId);

        List<AgingBucketDto> buckets = jdbcTemplate.query(sql, params, rs -> {
            List<AgingBucketDto> list = new ArrayList<>();
            if (rs.next()) {
                list.add(new AgingBucketDto("0–30 Days", rs.getLong("count_0_30"), rs.getBigDecimal("val_0_30") != null ? rs.getBigDecimal("val_0_30") : BigDecimal.ZERO));
                list.add(new AgingBucketDto("31–60 Days", rs.getLong("count_31_60"), rs.getBigDecimal("val_31_60") != null ? rs.getBigDecimal("val_31_60") : BigDecimal.ZERO));
                list.add(new AgingBucketDto("61–90 Days", rs.getLong("count_61_90"), rs.getBigDecimal("val_61_90") != null ? rs.getBigDecimal("val_61_90") : BigDecimal.ZERO));
                list.add(new AgingBucketDto("90+ Days", rs.getLong("count_90_plus"), rs.getBigDecimal("val_90_plus") != null ? rs.getBigDecimal("val_90_plus") : BigDecimal.ZERO));
            } else {
                list.add(new AgingBucketDto("0–30 Days", 0L, BigDecimal.ZERO));
                list.add(new AgingBucketDto("31–60 Days", 0L, BigDecimal.ZERO));
                list.add(new AgingBucketDto("61–90 Days", 0L, BigDecimal.ZERO));
                list.add(new AgingBucketDto("90+ Days", 0L, BigDecimal.ZERO));
            }
            return list;
        });
        return buckets != null ? buckets : Collections.emptyList();
    }

    public List<SupplierLeadTimeDto> getSupplierLeadTimes(Long tenantId) {
        String sql = """
            SELECT
                s.id as supplier_id,
                s.name as supplier_name,
                COUNT(po.id) as total_orders,
                COALESCE(SUM(CASE WHEN po.status IN ('RECEIVED', 'CLOSED') THEN 1 ELSE 0 END), 0) as completed_orders,
                COALESCE(AVG(CASE WHEN po.status IN ('RECEIVED', 'CLOSED') THEN (CAST(COALESCE(po.received_at, po.updated_at) AS DATE) - CAST(COALESCE(po.submitted_at, po.order_date) AS DATE)) ELSE NULL END), 0.0) as avg_lead_time
            FROM suppliers s
            JOIN purchase_orders po ON po.supplier_id = s.id
            WHERE po.tenant_id = :tenantId
            GROUP BY s.id, s.name
            ORDER BY s.name ASC
            """;
        MapSqlParameterSource params = new MapSqlParameterSource("tenantId", tenantId);

        List<SupplierLeadTimeDto> list = jdbcTemplate.query(sql, params, (rs, rowNum) -> new SupplierLeadTimeDto(
            rs.getLong("supplier_id"),
            rs.getString("supplier_name") != null ? rs.getString("supplier_name") : "Unknown Supplier",
            rs.getLong("total_orders"),
            rs.getLong("completed_orders"),
            rs.getDouble("avg_lead_time")
        ));
        return list != null ? list : Collections.emptyList();
    }

    public List<OpenPurchaseOrderProjection> getRecentOpenOrders(Long tenantId, int limit) {
        String sql = """
            SELECT
                po.correlation_id as po_number,
                po.supplier_id as supplier_id,
                s.name as supplier_name,
                po.order_date as order_date,
                po.total_amount as total_amount,
                po.status as status,
                (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(po.order_date AS DATE)) as age_in_days
            FROM purchase_orders po
            JOIN suppliers s ON s.id = po.supplier_id
            WHERE po.tenant_id = :tenantId
              AND po.status IN ('SUBMITTED', 'APPROVED', 'ORDERED', 'PARTIALLY_RECEIVED')
            ORDER BY po.order_date DESC
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("limit", limit);

        List<OpenPurchaseOrderProjection> list = jdbcTemplate.query(sql, params, (rs, rowNum) -> new OpenPurchaseOrderProjection(
            rs.getString("po_number") != null ? rs.getString("po_number") : "",
            rs.getLong("supplier_id"),
            rs.getString("supplier_name") != null ? rs.getString("supplier_name") : "Unknown Supplier",
            rs.getTimestamp("order_date") != null ? rs.getTimestamp("order_date").toInstant() : Instant.EPOCH,
            rs.getBigDecimal("total_amount") != null ? rs.getBigDecimal("total_amount") : BigDecimal.ZERO,
            rs.getString("status") != null ? rs.getString("status") : "",
            Math.max(0L, rs.getLong("age_in_days"))
        ));
        return list != null ? list : Collections.emptyList();
    }
}
