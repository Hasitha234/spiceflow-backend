package com.spiceflow.backend.dashboard.finance.repository;

import com.spiceflow.backend.dashboard.finance.dto.ReceivableAgingBucketDto;
import com.spiceflow.backend.dashboard.finance.dto.RecentFinancialTransactionDto;
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
 * Pure CQRS Read Model repository for the Finance Dashboard.
 * Executes read-only SQL aggregations against shops, purchase_orders, cash_collections, and goods_receipts.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FinanceDashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public record SummaryMetrics(
        BigDecimal totalReceivables,
        BigDecimal totalPayables,
        BigDecimal monthCollections,
        BigDecimal monthPoSpent
    ) {}

    public SummaryMetrics getSummaryMetrics(Long tenantId, Instant startOfMonth) {
        String sql = """
            SELECT
                (SELECT COALESCE(SUM(outstanding_loan), 0.00)
                 FROM shops
                 WHERE tenant_id = :tenantId AND deleted_at IS NULL) AS total_rec,
                (SELECT COALESCE(SUM(total_amount), 0.00)
                 FROM purchase_orders
                 WHERE tenant_id = :tenantId AND status IN ('SUBMITTED', 'APPROVED', 'ORDERED', 'PARTIALLY_RECEIVED')) AS total_pay,
                (SELECT COALESCE(SUM(amount), 0.00)
                 FROM cash_collections
                 WHERE tenant_id = :tenantId AND status = 'CONFIRMED' AND COALESCE(confirmed_at, created_at) >= :startOfMonth) AS month_coll,
                (SELECT COALESCE(SUM(total_accepted_value), 0.00)
                 FROM goods_receipts
                 WHERE tenant_id = :tenantId AND status = 'POSTED' AND COALESCE(posted_at, created_at) >= :startOfMonth) AS month_po
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("startOfMonth", java.sql.Timestamp.from(startOfMonth));

        SummaryMetrics metrics = jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> new SummaryMetrics(
            rs.getBigDecimal("total_rec") != null ? rs.getBigDecimal("total_rec") : BigDecimal.ZERO,
            rs.getBigDecimal("total_pay") != null ? rs.getBigDecimal("total_pay") : BigDecimal.ZERO,
            rs.getBigDecimal("month_coll") != null ? rs.getBigDecimal("month_coll") : BigDecimal.ZERO,
            rs.getBigDecimal("month_po") != null ? rs.getBigDecimal("month_po") : BigDecimal.ZERO
        ));
        return metrics != null ? metrics : new SummaryMetrics(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public List<ReceivableAgingBucketDto> getReceivablesAgingBuckets(Long tenantId) {
        String sql = """
            SELECT
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(COALESCE(updated_at, created_at) AS DATE)) <= 30 THEN 1 ELSE 0 END), 0) AS count_0_30,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(COALESCE(updated_at, created_at) AS DATE)) <= 30 THEN outstanding_loan ELSE 0.00 END), 0.00) AS val_0_30,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(COALESCE(updated_at, created_at) AS DATE)) BETWEEN 31 AND 60 THEN 1 ELSE 0 END), 0) AS count_31_60,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(COALESCE(updated_at, created_at) AS DATE)) BETWEEN 31 AND 60 THEN outstanding_loan ELSE 0.00 END), 0.00) AS val_31_60,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(COALESCE(updated_at, created_at) AS DATE)) BETWEEN 61 AND 90 THEN 1 ELSE 0 END), 0) AS count_61_90,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(COALESCE(updated_at, created_at) AS DATE)) BETWEEN 61 AND 90 THEN outstanding_loan ELSE 0.00 END), 0.00) AS val_61_90,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(COALESCE(updated_at, created_at) AS DATE)) > 90 THEN 1 ELSE 0 END), 0) AS count_90_plus,
                COALESCE(SUM(CASE WHEN (CAST(CURRENT_TIMESTAMP AS DATE) - CAST(COALESCE(updated_at, created_at) AS DATE)) > 90 THEN outstanding_loan ELSE 0.00 END), 0.00) AS val_90_plus
            FROM shops
            WHERE tenant_id = :tenantId AND deleted_at IS NULL AND outstanding_loan > 0
            """;
        MapSqlParameterSource params = new MapSqlParameterSource("tenantId", tenantId);

        List<ReceivableAgingBucketDto> buckets = jdbcTemplate.query(sql, params, rs -> {
            List<ReceivableAgingBucketDto> list = new ArrayList<>();
            if (rs.next()) {
                list.add(new ReceivableAgingBucketDto("0–30 Days", rs.getLong("count_0_30"), rs.getBigDecimal("val_0_30") != null ? rs.getBigDecimal("val_0_30") : BigDecimal.ZERO));
                list.add(new ReceivableAgingBucketDto("31–60 Days", rs.getLong("count_31_60"), rs.getBigDecimal("val_31_60") != null ? rs.getBigDecimal("val_31_60") : BigDecimal.ZERO));
                list.add(new ReceivableAgingBucketDto("61–90 Days", rs.getLong("count_61_90"), rs.getBigDecimal("val_61_90") != null ? rs.getBigDecimal("val_61_90") : BigDecimal.ZERO));
                list.add(new ReceivableAgingBucketDto("90+ Days", rs.getLong("count_90_plus"), rs.getBigDecimal("val_90_plus") != null ? rs.getBigDecimal("val_90_plus") : BigDecimal.ZERO));
            }
            return list;
        });
        return buckets != null ? buckets : Collections.emptyList();
    }

    public List<RecentFinancialTransactionDto> getRecentTransactions(Long tenantId, int limit) {
        String sql = """
            SELECT id, tx_type, ref_no, party_name, amount, payment_method, ts, status
            FROM (
                SELECT
                    cc.id AS id,
                    'COLLECTION' AS tx_type,
                    cc.collection_number AS ref_no,
                    COALESCE(s.name, 'Unknown Shop') AS party_name,
                    cc.amount AS amount,
                    cc.payment_method AS payment_method,
                    COALESCE(cc.confirmed_at, cc.created_at) AS ts,
                    cc.status AS status
                FROM cash_collections cc
                LEFT JOIN shops s ON cc.shop_id = s.id
                WHERE cc.tenant_id = :tenantId AND cc.status = 'CONFIRMED'
                UNION ALL
                SELECT
                    gr.id AS id,
                    'PO_RECEIPT' AS tx_type,
                    gr.receipt_number AS ref_no,
                    COALESCE(sup.name, 'Unknown Supplier') AS party_name,
                    gr.total_accepted_value AS amount,
                    'BANK_TRANSFER' AS payment_method,
                    COALESCE(gr.posted_at, gr.created_at) AS ts,
                    gr.status AS status
                FROM goods_receipts gr
                LEFT JOIN suppliers sup ON gr.supplier_id = sup.id
                WHERE gr.tenant_id = :tenantId AND gr.status = 'POSTED'
            ) combined
            WHERE ts IS NOT NULL
            ORDER BY ts DESC
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", tenantId)
            .addValue("limit", limit);

        List<RecentFinancialTransactionDto> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Instant ts = rs.getTimestamp("ts") != null ? rs.getTimestamp("ts").toInstant() : Instant.EPOCH;
            return new RecentFinancialTransactionDto(
                rs.getLong("id"),
                rs.getString("tx_type") != null ? rs.getString("tx_type") : "",
                rs.getString("ref_no") != null ? rs.getString("ref_no") : "",
                rs.getString("party_name") != null ? rs.getString("party_name") : "",
                rs.getBigDecimal("amount") != null ? rs.getBigDecimal("amount") : BigDecimal.ZERO,
                rs.getString("payment_method") != null ? rs.getString("payment_method") : "",
                ts,
                rs.getString("status") != null ? rs.getString("status") : ""
            );
        });
        return results != null ? results : Collections.emptyList();
    }
}
