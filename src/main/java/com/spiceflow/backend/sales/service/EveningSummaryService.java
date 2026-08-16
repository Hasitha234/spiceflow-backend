package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.repository.InventoryTransactionRepository;
import com.spiceflow.backend.inventory.repository.ProductRepository;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.sales.dto.request.EveningSummaryItemRequest;
import com.spiceflow.backend.sales.dto.request.EveningSummaryRequest;
import com.spiceflow.backend.sales.dto.response.EveningSummaryResponse;
import com.spiceflow.backend.sales.dto.response.StockAvailabilityResponse;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.EveningSummary;
import com.spiceflow.backend.sales.entity.EveningSummaryItem;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.mapper.EveningSummaryMapper;
import com.spiceflow.backend.sales.repository.DailyBalanceRepository;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.sales.repository.EveningSummaryRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EveningSummaryService {

    private final EveningSummaryRepository eveningSummaryRepository;
    private final TenantRepository tenantRepository;
    private final RepRepository repRepository;
    private final DriverRepository driverRepository;
    private final ProductRepository productRepository;
    private final EveningSummaryMapper eveningSummaryMapper;
    
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryLedgerService inventoryLedgerService;
    private final DailyBalanceRepository dailyBalanceRepository;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional
    @Retryable(
        retryFor = { DataAccessResourceFailureException.class, org.springframework.transaction.CannotCreateTransactionException.class },
        maxAttempts = 2,
        backoff = @Backoff(delay = 200)
    )
    public EveningSummaryResponse createEveningSummary(Long tenantId, EveningSummaryRequest request) {
        List<Long> productIds = request.items().stream()
                .map(EveningSummaryItemRequest::productId)
                .toList();
        Set<Long> uniqueProductIds = new HashSet<>(productIds);
        if (uniqueProductIds.size() != productIds.size()) {
            throw new BusinessRuleViolationException("Duplicate products are not allowed in an evening summary. Please merge quantities for the same product into a single line item.");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Rep rep = repRepository.findByIdAndTenantId(request.repId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));

        Driver driver = driverRepository.findByIdAndTenantId(request.driverId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        EveningSummary eveningSummary = EveningSummary.builder()
                .tenant(tenant)
                .rep(rep)
                .driver(driver)
                .summaryDate(request.summaryDate())
                .summaryNumber(generateSummaryNumber(tenantId, request.summaryDate()))
                .status("PENDING")
                .build();

        BigDecimal finalEstimateValue = BigDecimal.ZERO;

        for (EveningSummaryItemRequest itemRequest : request.items()) {
            Product product = productRepository.findByIdAndTenantId(itemRequest.productId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.productId()));

            BigDecimal unitPrice = resolveUnitPrice(product);
            BigDecimal estimateValue = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            EveningSummaryItem item = EveningSummaryItem.builder()
                    .eveningSummary(eveningSummary)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .estimateValue(estimateValue)
                    .build();

            eveningSummary.addItem(item);
            finalEstimateValue = finalEstimateValue.add(estimateValue);
        }

        eveningSummary.setFinalEstimateValue(finalEstimateValue);

        EveningSummary savedSummary = eveningSummaryRepository.save(eveningSummary);
        return eveningSummaryMapper.toResponse(savedSummary);
    }

    @Transactional
    public EveningSummaryResponse updateEveningSummary(Long tenantId, Long id, EveningSummaryRequest request) {
        List<Long> productIds = request.items().stream()
                .map(EveningSummaryItemRequest::productId)
                .toList();
        Set<Long> uniqueProductIds = new HashSet<>(productIds);
        if (uniqueProductIds.size() != productIds.size()) {
            throw new BusinessRuleViolationException("Duplicate products are not allowed in an evening summary. Please merge quantities for the same product into a single line item.");
        }

        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found");
        }

        EveningSummary eveningSummary = eveningSummaryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Evening Summary not found"));

        if (!"PENDING".equals(eveningSummary.getStatus()) || eveningSummary.isInventoryProcessed()) {
            throw new BusinessRuleViolationException("Only PENDING and unprocessed evening summaries can be updated.");
        }

        if (!request.summaryDate().equals(eveningSummary.getSummaryDate())) {
            throw new BusinessRuleViolationException(
                "Cannot change the summary date. Please delete this summary and create a new one.");
        }

        Rep rep = repRepository.findByIdAndTenantId(request.repId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));

        Driver driver = driverRepository.findByIdAndTenantId(request.driverId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        eveningSummary.setRep(rep);
        eveningSummary.setDriver(driver);

        eveningSummary.getItems().clear();
        entityManager.flush();

        BigDecimal finalEstimateValue = BigDecimal.ZERO;

        for (EveningSummaryItemRequest itemRequest : request.items()) {
            Product product = productRepository.findByIdAndTenantId(itemRequest.productId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.productId()));

            BigDecimal unitPrice = resolveUnitPrice(product);
            BigDecimal estimateValue = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            EveningSummaryItem item = EveningSummaryItem.builder()
                    .eveningSummary(eveningSummary)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .estimateValue(estimateValue)
                    .build();

            eveningSummary.addItem(item);
            finalEstimateValue = finalEstimateValue.add(estimateValue);
        }

        eveningSummary.setFinalEstimateValue(finalEstimateValue);

        EveningSummary savedSummary = eveningSummaryRepository.save(eveningSummary);
        return eveningSummaryMapper.toResponse(savedSummary);
    }

    @Transactional(readOnly = true)
    public Page<EveningSummaryResponse> getEveningSummaries(Long tenantId, String search, Long repId, Long driverId, LocalDate startDate, LocalDate endDate, String status, Pageable pageable) {
        return eveningSummaryRepository.findByFilters(tenantId, search, repId, driverId, startDate, endDate, status, pageable)
                .map(eveningSummaryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EveningSummaryResponse getEveningSummaryById(Long tenantId, Long id) {
        return eveningSummaryRepository.findByIdAndTenantId(id, tenantId)
                .map(eveningSummaryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Evening Summary not found"));
    }

    @Transactional(readOnly = true)
    public List<StockAvailabilityResponse> checkStockAvailability(Long tenantId, Long summaryId, Long warehouseId) {
        EveningSummary summary = eveningSummaryRepository.findByIdAndTenantId(summaryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Evening Summary not found"));

        var unused = warehouseRepository.findByIdAndTenantId(warehouseId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        List<StockAvailabilityResponse> results = new ArrayList<>();

        for (EveningSummaryItem item : summary.getItems()) {
            int available = inventoryItemRepository
                    .findByProductIdAndWarehouseIdAndTenantId(item.getProduct().getId(), warehouseId, tenantId)
                    .map(InventoryItem::getQuantityAvailable)
                    .orElse(0);

            int shortQty = Math.max(0, item.getQuantity() - available);

            results.add(new StockAvailabilityResponse(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    available,
                    shortQty,
                    available >= item.getQuantity()
            ));
        }
        return results;
    }

    @Transactional
    public void proceedEveningSummary(Long tenantId, Long id, Long warehouseId) {
        EveningSummary summary = eveningSummaryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Evening Summary not found"));

        if (summary.isInventoryProcessed()) {
            throw new BusinessRuleViolationException("Inventory has already been processed for this evening summary");
        }

        Warehouse deductionWarehouse = warehouseRepository.findByIdAndTenantId(warehouseId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        // First, run stock check — fail fast if any shortage
        List<StockAvailabilityResponse> stockCheck = checkStockAvailability(tenantId, id, warehouseId);
        boolean hasShortage = stockCheck.stream().anyMatch(s -> !s.sufficient());
        if (hasShortage) {
            throw new BusinessRuleViolationException("Insufficient stock. Check stock availability before proceeding.");
        }

        for (EveningSummaryItem item : summary.getItems()) {
            if (item.getQuantity() > 0) {
                InventoryItem inventoryItem = inventoryItemRepository
                        .findByProductIdAndWarehouseIdAndTenantId(item.getProduct().getId(), warehouseId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found")); // We already checked availability so it must exist

                inventoryItem.setQuantityAvailable(inventoryItem.getQuantityAvailable() - item.getQuantity());
                inventoryItemRepository.save(inventoryItem);

                inventoryLedgerService.recordMovement(
                        tenantId,
                        warehouseId,
                        item.getProduct().getId(),
                        InventoryMovementType.EVENING_SALE_DEDUCTION,
                        BigDecimal.valueOf(-item.getQuantity()),
                        item.getUnitPrice(),
                        summary.getSummaryNumber(),
                        "",
                        null,
                        Instant.now(),
                        "system"
                );

                InventoryTransaction tx = InventoryTransaction.builder()
                        .inventoryItem(inventoryItem)
                        .transactionType("EVENING_SALE_DEDUCTION")
                        .quantity(-item.getQuantity())
                        .referenceId(summary.getSummaryNumber())
                        .notes("Deducted sold items for evening summary")
                        .tenant(summary.getTenant())
                        .build();
                inventoryTransactionRepository.save(tx);
            }
        }
        
        summary.setStatus("SETTLED");
        summary.setInventoryProcessed(true);
        summary.setDeductionWarehouse(deductionWarehouse);
        eveningSummaryRepository.save(summary);
    }

    @Transactional
    @SuppressWarnings("NullAway")
    public void undoProceedEveningSummary(Long tenantId, Long id) {
        EveningSummary summary = eveningSummaryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Evening Summary not found"));

        if (!summary.isInventoryProcessed()) {
            throw new BusinessRuleViolationException("Cannot undo: inventory has not been processed for this evening summary");
        }

        if (summary.getDeductionWarehouse() == null) {
            throw new BusinessRuleViolationException("Cannot undo: deduction warehouse is unknown");
        }

        Long deductionWarehouseId = summary.getDeductionWarehouse().getId();

        for (EveningSummaryItem item : summary.getItems()) {
            if (item.getQuantity() > 0) {
                InventoryItem inventoryItem = inventoryItemRepository
                        .findByProductIdAndWarehouseIdAndTenantId(item.getProduct().getId(), deductionWarehouseId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

                inventoryItem.setQuantityAvailable(inventoryItem.getQuantityAvailable() + item.getQuantity());
                inventoryItemRepository.save(inventoryItem);

                inventoryLedgerService.recordMovement(
                        tenantId,
                        deductionWarehouseId,
                        item.getProduct().getId(),
                        InventoryMovementType.EVENING_SALE_REVERSAL,
                        BigDecimal.valueOf(item.getQuantity()),
                        item.getUnitPrice(),
                        summary.getSummaryNumber() + "-REVERSAL",
                        "",
                        null,
                        Instant.now(),
                        "system"
                );

                InventoryTransaction tx = InventoryTransaction.builder()
                        .inventoryItem(inventoryItem)
                        .transactionType("EVENING_SALE_REVERSAL")
                        .quantity(item.getQuantity())
                        .referenceId(summary.getSummaryNumber() + "-REVERSAL")
                        .notes("Reversed evening summary deduction")
                        .tenant(summary.getTenant())
                        .build();
                inventoryTransactionRepository.save(tx);
            }
        }

        summary.setInventoryProcessed(false);
        summary.setDeductionWarehouse(null);

        // Only reset to PENDING if the daily balance hasn't settled it
        if (!dailyBalanceRepository.existsByTenantIdAndBalanceDate(tenantId, summary.getSummaryDate())) {
            summary.setStatus("PENDING");
        }

        eveningSummaryRepository.save(summary);
    }

    @Transactional
    public void deleteEveningSummary(Long tenantId, Long id) {
        EveningSummary summary = eveningSummaryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Evening Summary not found"));

        if (!"PENDING".equals(summary.getStatus())) {
            throw new BusinessRuleViolationException("Only PENDING evening summaries can be deleted");
        }

        eveningSummaryRepository.delete(summary);
    }

    private String generateSummaryNumber(Long tenantId, LocalDate date) {
        int maxSeq = eveningSummaryRepository.findMaxSequenceNumberForDate(tenantId, date);
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("ES-%s-%03d", dateStr, maxSeq + 1);
    }

    private BigDecimal resolveUnitPrice(Product product) {
        if (product.getRatePerSoldUnit() != null && product.getRatePerSoldUnit().compareTo(BigDecimal.ZERO) > 0) {
            return product.getRatePerSoldUnit();
        }
        if (product.getBasePrice() != null && product.getBasePrice().compareTo(BigDecimal.ZERO) > 0) {
            return product.getBasePrice();
        }
        return BigDecimal.ZERO;
    }
}
