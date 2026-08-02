package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.sales.dto.response.DailyBalanceResponse;
import com.spiceflow.backend.sales.entity.Bill;
import com.spiceflow.backend.sales.entity.CancelSummary;
import com.spiceflow.backend.sales.entity.DailyBalance;
import com.spiceflow.backend.sales.entity.MorningSummary;
import com.spiceflow.backend.sales.repository.BillRepository;
import com.spiceflow.backend.sales.repository.CancelSummaryRepository;
import com.spiceflow.backend.sales.repository.DailyBalanceRepository;
import com.spiceflow.backend.sales.repository.MorningSummaryRepository;
import com.spiceflow.backend.auth.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyBalanceService {

    private final DailyBalanceRepository dailyBalanceRepository;
    private final MorningSummaryRepository morningSummaryRepository;
    private final CancelSummaryRepository cancelSummaryRepository;
    private final BillRepository billRepository;
    private final TenantRepository tenantRepository;

    public DailyBalanceResponse getDailyBalance(Long tenantId, LocalDate date) {
        log.info("Fetching daily balance for tenant {} on {}", tenantId, date);

        List<MorningSummary> morningSummaries = morningSummaryRepository.findByTenantIdAndSummaryDate(tenantId, date);
        List<CancelSummary> cancelSummaries = cancelSummaryRepository.findByTenantIdAndSummaryDate(tenantId, date);
        List<Bill> bills = billRepository.findByTenantIdAndBillDate(tenantId, date);

        BigDecimal morningTotal = morningSummaries.stream()
                .map(MorningSummary::getFinalEstimateValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cancelTotal = cancelSummaries.stream()
                .map(CancelSummary::getFinalEstimateValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netDispatch = morningTotal.subtract(cancelTotal);

        BigDecimal billsTotal = bills.stream()
                .map(b -> b.getNetTotal().add(b.getFreeItemsValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean isBalanced = netDispatch.compareTo(billsTotal) == 0;

        return dailyBalanceRepository.findByTenantIdAndBalanceDate(tenantId, date)
                .map(balance -> DailyBalanceResponse.builder()
                        .date(balance.getBalanceDate())
                        .morningSummaryTotal(balance.getMorningSummaryTotal())
                        .cancelSummaryTotal(balance.getCancelSummaryTotal())
                        .netDispatchTotal(balance.getNetDispatchTotal())
                        .billsTotal(balance.getBillsTotal())
                        .isBalanced(true)
                        .status(balance.getStatus())
                        .build())
                .orElseGet(() -> DailyBalanceResponse.builder()
                        .date(date)
                        .morningSummaryTotal(morningTotal)
                        .cancelSummaryTotal(cancelTotal)
                        .netDispatchTotal(netDispatch)
                        .billsTotal(billsTotal)
                        .isBalanced(isBalanced)
                        .status("PENDING")
                        .build());
    }

    @Transactional
    public DailyBalanceResponse proceedDailyBalance(Long tenantId, LocalDate date) {
        log.info("Proceeding daily balance for tenant {} on {}", tenantId, date);

        if (dailyBalanceRepository.existsByTenantIdAndBalanceDate(tenantId, date)) {
            throw new IllegalArgumentException("Daily balance is already proceeded for this date.");
        }

        List<MorningSummary> morningSummaries = morningSummaryRepository.findByTenantIdAndSummaryDate(tenantId, date);
        List<CancelSummary> cancelSummaries = cancelSummaryRepository.findByTenantIdAndSummaryDate(tenantId, date);
        List<Bill> bills = billRepository.findByTenantIdAndBillDate(tenantId, date);

        BigDecimal morningTotal = morningSummaries.stream()
                .map(MorningSummary::getFinalEstimateValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cancelTotal = cancelSummaries.stream()
                .map(CancelSummary::getFinalEstimateValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netDispatch = morningTotal.subtract(cancelTotal);

        BigDecimal billsTotal = bills.stream()
                .map(b -> b.getNetTotal().add(b.getFreeItemsValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (netDispatch.compareTo(billsTotal) != 0) {
            throw new IllegalArgumentException("Cannot proceed. Net Dispatch does not match Bills Total.");
        }

        // Auto-settle PENDING summaries
        for (MorningSummary ms : morningSummaries) {
            if ("PENDING".equals(ms.getStatus())) {
                ms.setStatus("SETTLED");
                morningSummaryRepository.save(ms);
            }
        }
        for (CancelSummary cs : cancelSummaries) {
            if ("PENDING".equals(cs.getStatus())) {
                cs.setStatus("SETTLED");
                cancelSummaryRepository.save(cs);
            }
        }

        DailyBalance newBalance = DailyBalance.builder()
                .tenant(tenantRepository.getReferenceById(tenantId))
                .balanceDate(date)
                .morningSummaryTotal(morningTotal)
                .cancelSummaryTotal(cancelTotal)
                .netDispatchTotal(netDispatch)
                .billsTotal(billsTotal)
                .status("BALANCED")
                .build();

        dailyBalanceRepository.save(newBalance);

        return DailyBalanceResponse.builder()
                .date(date)
                .morningSummaryTotal(morningTotal)
                .cancelSummaryTotal(cancelTotal)
                .netDispatchTotal(netDispatch)
                .billsTotal(billsTotal)
                .isBalanced(true)
                .status("BALANCED")
                .build();
    }

    @Transactional
    public DailyBalanceResponse undoDailyBalance(Long tenantId, LocalDate date) {
        log.info("Undoing daily balance for tenant {} on {}", tenantId, date);

        DailyBalance balance = dailyBalanceRepository.findByTenantIdAndBalanceDate(tenantId, date)
                .orElseThrow(() -> new ResourceNotFoundException("Daily balance not found for this date."));

        dailyBalanceRepository.delete(balance);

        List<MorningSummary> morningSummaries = morningSummaryRepository.findByTenantIdAndSummaryDate(tenantId, date);
        for (MorningSummary ms : morningSummaries) {
            if ("SETTLED".equals(ms.getStatus())) {
                ms.setStatus("PENDING");
                morningSummaryRepository.save(ms);
            }
        }

        List<CancelSummary> cancelSummaries = cancelSummaryRepository.findByTenantIdAndSummaryDate(tenantId, date);
        for (CancelSummary cs : cancelSummaries) {
            if ("SETTLED".equals(cs.getStatus())) {
                cs.setStatus("PENDING");
                cancelSummaryRepository.save(cs);
            }
        }

        return getDailyBalance(tenantId, date);
    }
}
