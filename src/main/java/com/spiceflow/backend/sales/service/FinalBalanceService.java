package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.sales.dto.request.FinalBalanceRequest;
import com.spiceflow.backend.sales.dto.response.FinalBalanceCalculationResponse;
import com.spiceflow.backend.sales.dto.response.FinalBalanceResponse;
import com.spiceflow.backend.sales.entity.Bill;
import com.spiceflow.backend.sales.entity.CancelSummary;
import com.spiceflow.backend.sales.entity.FinalBalance;
import com.spiceflow.backend.sales.entity.MorningSummary;
import com.spiceflow.backend.sales.repository.BillRepository;
import com.spiceflow.backend.sales.repository.CancelSummaryRepository;
import com.spiceflow.backend.sales.repository.FinalBalanceRepository;
import com.spiceflow.backend.sales.repository.MorningSummaryRepository;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FinalBalanceService {

    private final FinalBalanceRepository finalBalanceRepository;
    private final MorningSummaryRepository morningSummaryRepository;
    private final CancelSummaryRepository cancelSummaryRepository;
    private final BillRepository billRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public Page<FinalBalanceResponse> getFinalBalances(Long tenantId, Long repId, LocalDate balanceDate, Pageable pageable) {
        return finalBalanceRepository.findByTenantAndFilters(tenantId, repId, balanceDate, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public FinalBalanceCalculationResponse calculateBalance(Long tenantId, FinalBalanceRequest request) {
        // Validate rep and tenant
        User rep = userRepository.findById(request.getRepId())
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rep not found"));

        // Fetch related data
        List<MorningSummary> morningSummaries = morningSummaryRepository
                .findByTenantIdAndRepIdAndSummaryDate(tenantId, rep.getId(), request.getBalanceDate());
        List<CancelSummary> cancelSummaries = cancelSummaryRepository
                .findByTenantIdAndRepIdAndSummaryDate(tenantId, rep.getId(), request.getBalanceDate());
        List<Bill> bills = billRepository
                .findByTenantIdAndRepIdAndBillDate(tenantId, rep.getId(), request.getBalanceDate());

        // Aggregate values
        BigDecimal morningValue = morningSummaries.stream()
                .map(MorningSummary::getFinalEstimateValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cancelValue = cancelSummaries.stream()
                .map(CancelSummary::getFinalEstimateValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal billCollections = bills.stream()
                .map(Bill::getFinalTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate math
        // Math = Morning Summary - (Cancel Summary + Total Bill Collections)
        BigDecimal mismatchValue = morningValue.subtract(cancelValue.add(billCollections));
        
        String status = mismatchValue.compareTo(BigDecimal.ZERO) == 0 ? 
                FinalBalance.BalanceStatus.BALANCED.name() : 
                FinalBalance.BalanceStatus.MISMATCHED.name();

        return FinalBalanceCalculationResponse.builder()
                .repId(rep.getId())
                .driverId(request.getDriverId())
                .balanceDate(request.getBalanceDate())
                .morningSummaryValue(morningValue)
                .cancelSummaryValue(cancelValue)
                .totalBillCollections(billCollections)
                .mismatchValue(mismatchValue)
                .status(status)
                .build();
    }

    @Transactional
    public FinalBalanceResponse saveFinalBalance(Long tenantId, FinalBalanceRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));

        if (finalBalanceRepository.existsByTenantIdAndRepIdAndBalanceDate(tenantId, request.getRepId(), request.getBalanceDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Final balance for this rep on this date already exists");
        }

        User rep = userRepository.findById(request.getRepId())
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rep not found"));

        Driver driver = null;
        if (request.getDriverId() != null) {
            driver = driverRepository.findByIdAndTenantId(request.getDriverId(), tenantId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
        }

        FinalBalanceCalculationResponse calculation = calculateBalance(tenantId, request);

        FinalBalance finalBalance = new FinalBalance();
        finalBalance.setTenant(tenant);
        finalBalance.setRep(rep);
        finalBalance.setDriver(driver);
        finalBalance.setBalanceDate(request.getBalanceDate());
        finalBalance.setMorningSummaryValue(calculation.getMorningSummaryValue());
        finalBalance.setCancelSummaryValue(calculation.getCancelSummaryValue());
        finalBalance.setTotalBillCollections(calculation.getTotalBillCollections());
        finalBalance.setMismatchValue(calculation.getMismatchValue());
        finalBalance.setStatus(FinalBalance.BalanceStatus.valueOf(Objects.requireNonNull(calculation.getStatus())));
        finalBalance.setRemarks(request.getRemarks());

        FinalBalance saved = finalBalanceRepository.save(finalBalance);
        return mapToResponse(saved);
    }

    private FinalBalanceResponse mapToResponse(FinalBalance finalBalance) {
        return FinalBalanceResponse.builder()
                .id(finalBalance.getId())
                .repId(finalBalance.getRep().getId())
                .repName(finalBalance.getRep().getName())
                .driverId(finalBalance.getDriver() != null ? finalBalance.getDriver().getId() : null)
                .driverName(finalBalance.getDriver() != null ? finalBalance.getDriver().getName() : null)
                .balanceDate(finalBalance.getBalanceDate())
                .morningSummaryValue(finalBalance.getMorningSummaryValue())
                .cancelSummaryValue(finalBalance.getCancelSummaryValue())
                .totalBillCollections(finalBalance.getTotalBillCollections())
                .mismatchValue(finalBalance.getMismatchValue())
                .status(finalBalance.getStatus().name())
                .remarks(finalBalance.getRemarks())
                .createdAt(finalBalance.getCreatedAt())
                .build();
    }
}
