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

import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.repository.InventoryTransactionRepository;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import java.time.Instant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.JpaSystemException;

@Service
@RequiredArgsConstructor
public class CancelSummaryService {

    private final CancelSummaryRepository cancelSummaryRepository;
    private final TenantRepository tenantRepository;
    private final RepRepository repRepository;
    private final DriverRepository driverRepository;
    private final ProductRepository productRepository;
    private final CancelSummaryMapper cancelSummaryMapper;
    
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryLedgerService inventoryLedgerService;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional
    @Retryable(
        retryFor = { DataAccessResourceFailureException.class, org.springframework.transaction.CannotCreateTransactionException.class },
        maxAttempts = 2,
        backoff = @Backoff(delay = 200)
    )
    public CancelSummaryResponse createCancelSummary(Long tenantId, CancelSummaryRequest request) {
        java.util.List<Long> productIds = request.items().stream()
                .map(CancelSummaryItemRequest::productId)
                .toList();
        java.util.Set<Long> uniqueProductIds = new java.util.HashSet<>(productIds);
        if (uniqueProductIds.size() != productIds.size()) {
            throw new BusinessRuleViolationException("Duplicate products are not allowed in a cancel summary. Please merge quantities for the same product into a single line item.");
        }

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

            BigDecimal unitPrice = resolveUnitPrice(product);
            BigDecimal estimateValue = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            CancelSummaryItem item = CancelSummaryItem.builder()
                    .cancelSummary(cancelSummary)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .estimateValue(estimateValue)
                    .build();

            cancelSummary.addItem(item);
            finalEstimateValue = finalEstimateValue.add(estimateValue);
        }

        cancelSummary.setFinalEstimateValue(finalEstimateValue);

        CancelSummary savedSummary = cancelSummaryRepository.save(cancelSummary);
        return cancelSummaryMapper.toResponse(savedSummary);
    }

    @Transactional
    public CancelSummaryResponse updateCancelSummary(Long tenantId, Long id, CancelSummaryRequest request) {
        java.util.List<Long> productIds = request.items().stream()
                .map(CancelSummaryItemRequest::productId)
                .toList();
        java.util.Set<Long> uniqueProductIds = new java.util.HashSet<>(productIds);
        if (uniqueProductIds.size() != productIds.size()) {
            throw new BusinessRuleViolationException("Duplicate products are not allowed in a cancel summary. Please merge quantities for the same product into a single line item.");
        }

        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found");
        }

        CancelSummary cancelSummary = cancelSummaryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancel Summary not found"));

        if (!"PENDING".equals(cancelSummary.getStatus())) {
            throw new BusinessRuleViolationException("Only PENDING cancel summaries can be updated.");
        }

        if (!request.summaryDate().equals(cancelSummary.getSummaryDate())) {
            throw new BusinessRuleViolationException(
                "Cannot change the summary date. Please delete this summary and create a new one.");
        }

        Rep rep = repRepository.findByIdAndTenantId(request.repId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));

        Driver driver = driverRepository.findByIdAndTenantId(request.driverId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        cancelSummary.setRep(rep);
        cancelSummary.setDriver(driver);

        cancelSummary.getItems().clear();
        entityManager.flush();

        BigDecimal finalEstimateValue = BigDecimal.ZERO;

        for (CancelSummaryItemRequest itemRequest : request.items()) {
            Product product = productRepository.findByIdAndTenantId(itemRequest.productId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.productId()));

            BigDecimal unitPrice = resolveUnitPrice(product);
            BigDecimal estimateValue = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            CancelSummaryItem item = CancelSummaryItem.builder()
                    .cancelSummary(cancelSummary)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .estimateValue(estimateValue)
                    .build();

            cancelSummary.addItem(item);
            finalEstimateValue = finalEstimateValue.add(estimateValue);
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

    @Transactional
    @SuppressWarnings("NullAway")
    public void proceedCancelSummary(Long tenantId, Long id, Long returnWarehouseId) {
        CancelSummary summary = cancelSummaryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancel Summary not found"));

        if (!"PENDING".equals(summary.getStatus())) {
            throw new BusinessRuleViolationException("Only PENDING cancel summaries can be processed");
        }

        Warehouse returnWarehouse = warehouseRepository.findByIdAndTenantId(returnWarehouseId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        for (CancelSummaryItem item : summary.getItems()) {
            if (item.getQuantity() > 0) {
                InventoryItem inventoryItem = inventoryItemRepository
                        .findByProductIdAndWarehouseIdAndTenantId(item.getProduct().getId(), returnWarehouseId, tenantId)
                        .orElseGet(() -> {
                            InventoryItem newItem = InventoryItem.builder()
                                    .tenant(summary.getTenant())
                                    .warehouse(returnWarehouse)
                                    .product(item.getProduct())
                                    .quantityAvailable(0)
                                    .build();
                            return inventoryItemRepository.save(newItem);
                        });

                inventoryItem.setQuantityAvailable(inventoryItem.getQuantityAvailable() + item.getQuantity());
                inventoryItemRepository.save(inventoryItem);

                inventoryLedgerService.recordMovement(
                        tenantId,
                        returnWarehouseId,
                        item.getProduct().getId(),
                        InventoryMovementType.CANCEL_RETURN_RECEIPT,
                        BigDecimal.valueOf(item.getQuantity()),
                        item.getUnitPrice(),
                        summary.getSummaryNumber(),
                        "",
                        null,
                        Instant.now(),
                        "system"
                );

                InventoryTransaction tx = InventoryTransaction.builder()
                        .inventoryItem(inventoryItem)
                        .transactionType("CANCEL_RETURN_RECEIPT")
                        .quantity(item.getQuantity())
                        .referenceId(summary.getSummaryNumber())
                        .notes("Added return for cancel summary")
                        .tenant(summary.getTenant())
                        .build();
                inventoryTransactionRepository.save(tx);
            }
        }

        summary.setStatus("SETTLED");
        summary.setReturnWarehouse(returnWarehouse);
        cancelSummaryRepository.save(summary);
    }

    @Transactional
    @SuppressWarnings("NullAway")
    public void undoProceedCancelSummary(Long tenantId, Long id) {
        CancelSummary summary = cancelSummaryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancel Summary not found"));

        if (!"SETTLED".equals(summary.getStatus())) {
            throw new BusinessRuleViolationException("Only SETTLED cancel summaries can be undone");
        }

        if (summary.getReturnWarehouse() == null) {
            throw new BusinessRuleViolationException("Cannot undo: return warehouse is unknown");
        }

        Long returnWarehouseId = summary.getReturnWarehouse().getId();

        for (CancelSummaryItem item : summary.getItems()) {
            if (item.getQuantity() > 0) {
                InventoryItem inventoryItem = inventoryItemRepository
                        .findByProductIdAndWarehouseIdAndTenantId(item.getProduct().getId(), returnWarehouseId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

                inventoryItem.setQuantityAvailable(inventoryItem.getQuantityAvailable() - item.getQuantity());
                inventoryItemRepository.save(inventoryItem);

                inventoryLedgerService.recordMovement(
                        tenantId,
                        returnWarehouseId,
                        item.getProduct().getId(),
                        InventoryMovementType.CANCEL_RETURN_REVERSAL,
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
                        .transactionType("CANCEL_RETURN_REVERSAL")
                        .quantity(-item.getQuantity())
                        .referenceId(summary.getSummaryNumber() + "-REVERSAL")
                        .notes("Undone cancel summary return")
                        .tenant(summary.getTenant())
                        .build();
                inventoryTransactionRepository.save(tx);
            }
        }

        summary.setStatus("PENDING");
        summary.setReturnWarehouse(null);
        cancelSummaryRepository.save(summary);
    }

    @Transactional
    public void deleteCancelSummary(Long tenantId, Long id) {
        CancelSummary summary = cancelSummaryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancel Summary not found"));

        if (!"PENDING".equals(summary.getStatus())) {
            throw new BusinessRuleViolationException("Only PENDING cancel summaries can be deleted");
        }

        cancelSummaryRepository.delete(summary);
    }

    private String generateSummaryNumber(Long tenantId, LocalDate date) {
        int maxSeq = cancelSummaryRepository.findMaxSequenceNumberForDate(tenantId, date);
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("CS-%s-%03d", dateStr, maxSeq + 1);
    }

    /**
     * Resolves the unit price for a product, preferring ratePerSoldUnit over basePrice.
     * Treats both null and zero as "not set" — mirrors the frontend's JavaScript falsy behavior
     * where {@code 0 || fallback} skips zero values.
     */
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
