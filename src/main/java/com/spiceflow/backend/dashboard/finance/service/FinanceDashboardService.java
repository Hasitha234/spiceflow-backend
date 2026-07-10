package com.spiceflow.backend.dashboard.finance.service;

import com.spiceflow.backend.dashboard.finance.dto.FinanceDashboardResponse;
import com.spiceflow.backend.dashboard.finance.dto.ReceivableAgingBucketDto;
import com.spiceflow.backend.dashboard.finance.dto.RecentFinancialTransactionDto;
import com.spiceflow.backend.dashboard.finance.repository.FinanceDashboardRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pure CQRS Read Model service orchestrating queries for the Finance Dashboard.
 * Operates strictly read-only without transaction boundary entanglement or entity mutation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceDashboardService {

    private final FinanceDashboardRepository repository;

    public FinanceDashboardResponse getDashboard(Long tenantId, int limit) {
        log.debug("Fetching finance dashboard projection for tenantId={}, limit={}", tenantId, limit);

        Instant startOfMonth = ZonedDateTime.now(ZoneOffset.UTC)
            .withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();

        FinanceDashboardRepository.SummaryMetrics metrics = repository.getSummaryMetrics(tenantId, startOfMonth);
        List<ReceivableAgingBucketDto> agingBuckets = repository.getReceivablesAgingBuckets(tenantId);
        List<RecentFinancialTransactionDto> recentTransactions = repository.getRecentTransactions(tenantId, limit);

        return new FinanceDashboardResponse(
            metrics != null && metrics.totalReceivables() != null ? metrics.totalReceivables() : java.math.BigDecimal.ZERO,
            metrics != null && metrics.totalPayables() != null ? metrics.totalPayables() : java.math.BigDecimal.ZERO,
            (metrics != null && metrics.monthCollections() != null ? metrics.monthCollections() : java.math.BigDecimal.ZERO)
                .subtract(metrics != null && metrics.monthPoSpent() != null ? metrics.monthPoSpent() : java.math.BigDecimal.ZERO),
            metrics != null && metrics.monthCollections() != null ? metrics.monthCollections() : java.math.BigDecimal.ZERO,
            agingBuckets,
            recentTransactions
        );
    }
}
