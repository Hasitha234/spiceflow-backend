package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.repository.ProductRepository;
import com.spiceflow.backend.sales.dto.request.CancelSummaryRequest;
import com.spiceflow.backend.sales.dto.request.CancelSummaryItemRequest;
import com.spiceflow.backend.sales.dto.response.CancelSummaryResponse;
import com.spiceflow.backend.sales.entity.CancelSummary;
import com.spiceflow.backend.sales.entity.CancelSummaryItem;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.mapper.CancelSummaryMapper;
import com.spiceflow.backend.sales.repository.CancelSummaryRepository;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CancelSummaryService {

    private final CancelSummaryRepository cancelSummaryRepository;
    private final TenantRepository tenantRepository;
    private final RepRepository repRepository;
    private final DriverRepository driverRepository;
    private final ProductRepository productRepository;
    private final CancelSummaryMapper cancelSummaryMapper;

    @Transactional
    public CancelSummaryResponse createCancelSummary(Long tenantId, CancelSummaryRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Rep rep = repRepository.findByIdAndTenantId(request.repId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));

        Driver driver = driverRepository.findByIdAndTenantId(request.driverId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        CancelSummary cancelSummary = CancelSummary.builder()
                .tenant(tenant)
                .rep(rep)
                .driver(driver)
                .summaryDate(request.summaryDate())
                .summaryNumber(generateSummaryNumber(tenantId, request.summaryDate()))
                .status("PENDING")
                .build();

        BigDecimal finalEstimateValue = BigDecimal.ZERO;

        for (CancelSummaryItemRequest itemRequest : request.items()) {
            Product product = productRepository.findByIdAndTenantId(itemRequest.productId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.productId()));

            CancelSummaryItem item = CancelSummaryItem.builder()
                    .cancelSummary(cancelSummary)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .estimateValue(itemRequest.estimateValue())
                    .build();

            cancelSummary.addItem(item);
            finalEstimateValue = finalEstimateValue.add(itemRequest.estimateValue());
        }

        cancelSummary.setFinalEstimateValue(finalEstimateValue);

        CancelSummary savedSummary = cancelSummaryRepository.save(cancelSummary);
        return cancelSummaryMapper.toResponse(savedSummary);
    }

    @Transactional(readOnly = true)
    public Page<CancelSummaryResponse> getCancelSummaries(Long tenantId, String search, Long repId, Long driverId, LocalDate startDate, LocalDate endDate, String status, Pageable pageable) {
        return cancelSummaryRepository.findByFilters(tenantId, search, repId, driverId, startDate, endDate, status, pageable)
                .map(cancelSummaryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CancelSummaryResponse getCancelSummaryById(Long tenantId, Long id) {
        return cancelSummaryRepository.findByIdAndTenantId(id, tenantId)
                .map(cancelSummaryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cancel Summary not found"));
    }

    @Transactional
    public CancelSummaryResponse updateCancelSummaryStatus(Long tenantId, Long id, String status) {
        CancelSummary summary = cancelSummaryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancel Summary not found"));

        summary.setStatus(status);
        CancelSummary savedSummary = cancelSummaryRepository.save(summary);
        return cancelSummaryMapper.toResponse(savedSummary);
    }

    private String generateSummaryNumber(Long tenantId, LocalDate date) {
        int maxSeq = cancelSummaryRepository.findMaxSequenceNumberForDate(tenantId, date);
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("CS-%s-%03d", dateStr, maxSeq + 1);
    }
}
