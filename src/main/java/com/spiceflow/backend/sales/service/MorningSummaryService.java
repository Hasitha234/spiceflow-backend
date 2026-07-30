package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.repository.ProductRepository;
import com.spiceflow.backend.sales.dto.request.MorningSummaryRequest;
import com.spiceflow.backend.sales.dto.response.MorningSummaryResponse;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.MorningSummary;
import com.spiceflow.backend.sales.entity.MorningSummaryItem;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.sales.repository.MorningSummaryRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MorningSummaryService {

    private final MorningSummaryRepository morningSummaryRepository;
    private final TenantRepository tenantRepository;
    private final RepRepository repRepository;
    private final DriverRepository driverRepository;
    private final ProductRepository productRepository;

    @Transactional
    public MorningSummaryResponse createMorningSummary(Long tenantId, MorningSummaryRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Rep rep = repRepository.findByIdAndTenantId(request.repId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));

        Driver driver = driverRepository.findByIdAndTenantId(request.driverId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        MorningSummary morningSummary = MorningSummary.builder()
                .tenant(tenant)
                .rep(rep)
                .driver(driver)
                .summaryDate(request.summaryDate())
                .summaryNumber(generateSummaryNumber(tenantId))
                .status("PENDING")
                .build();

        BigDecimal finalEstimateValue = BigDecimal.ZERO;

        for (MorningSummaryRequest.MorningSummaryItemRequest itemRequest : request.items()) {
            Product product = productRepository.findByIdAndTenantId(itemRequest.productId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.productId()));

            BigDecimal unitPrice = product.getRatePerSoldUnit();
            BigDecimal estimateValue = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            MorningSummaryItem item = MorningSummaryItem.builder()
                    .tenant(tenant)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .estimateValue(estimateValue)
                    .expectedReturnAmount(itemRequest.expectedReturnAmount() != null ? itemRequest.expectedReturnAmount() : 0)
                    .expectedReturnPrice(itemRequest.expectedReturnPrice() != null ? itemRequest.expectedReturnPrice() : BigDecimal.ZERO)
                    .build();

            morningSummary.addItem(item);
            finalEstimateValue = finalEstimateValue.add(estimateValue);
        }

        morningSummary.setFinalEstimateValue(finalEstimateValue);
        
        MorningSummary savedSummary = morningSummaryRepository.save(morningSummary);
        return mapToResponse(savedSummary);
    }

    @Transactional(readOnly = true)
    public Page<MorningSummaryResponse> getAllSummaries(Long tenantId, Pageable pageable) {
        return morningSummaryRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public MorningSummaryResponse getSummaryById(Long tenantId, Long summaryId) {
        MorningSummary summary = morningSummaryRepository.findByIdAndTenantId(summaryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Morning Summary not found"));
        return mapToResponse(summary);
    }

    private String generateSummaryNumber(Long tenantId) {
        return morningSummaryRepository.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)
                .map(summary -> {
                    String lastNumber = summary.getSummaryNumber();
                    try {
                        int num = Integer.parseInt(lastNumber.replace("MS-", ""));
                        return String.format("MS-%04d", num + 1);
                    } catch (Exception e) {
                        return "MS-0001";
                    }
                })
                .orElse("MS-0001");
    }

    private MorningSummaryResponse mapToResponse(MorningSummary summary) {
        List<MorningSummaryResponse.MorningSummaryItemResponse> itemResponses = summary.getItems().stream()
                .map(item -> MorningSummaryResponse.MorningSummaryItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .estimateValue(item.getEstimateValue())
                        .expectedReturnAmount(item.getExpectedReturnAmount())
                        .expectedReturnPrice(item.getExpectedReturnPrice())
                        .build())
                .collect(Collectors.toList());

        return MorningSummaryResponse.builder()
                .id(summary.getId())
                .summaryNumber(summary.getSummaryNumber())
                .summaryDate(summary.getSummaryDate())
                .finalEstimateValue(summary.getFinalEstimateValue())
                .status(summary.getStatus())
                .repId(summary.getRep().getId())
                .repName(summary.getRep().getName())
                .driverId(summary.getDriver().getId())
                .driverName(summary.getDriver().getName())
                .items(itemResponses)
                .build();
    }
}
