package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.common.exception.InvalidReferenceException;
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
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.repository.InventoryTransactionRepository;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.ledger.service.InventoryLedgerService;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.sales.dto.response.DeductInventoryPreCheckResponse;
import com.spiceflow.backend.sales.dto.response.DeductInventoryPreCheckResponse.ItemAvailability;
import java.time.Instant;
import java.util.ArrayList;

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
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryLedgerService inventoryLedgerService;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional
    public MorningSummaryResponse createMorningSummary(Long tenantId, MorningSummaryRequest request) {
        List<MorningSummaryRequest.MorningSummaryItemRequest> items = request.items() != null ? request.items() : List.of();
        if (!items.isEmpty()) {
            List<Long> productIds = items.stream()
                    .map(MorningSummaryRequest.MorningSummaryItemRequest::productId)
                    .toList();
            java.util.Set<Long> uniqueProductIds = new java.util.HashSet<>(productIds);
            if (uniqueProductIds.size() != productIds.size()) {
                throw new BusinessRuleViolationException("Duplicate products are not allowed in a morning summary. Please merge quantities for the same product into a single line item.");
            }
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Rep rep = repRepository.findByIdAndTenantId(request.repId(), tenantId)
                .orElseThrow(() -> new InvalidReferenceException("Rep not found"));

        Driver driver = driverRepository.findByIdAndTenantId(request.driverId(), tenantId)
                .orElseThrow(() -> new InvalidReferenceException("Driver not found"));

        MorningSummary morningSummary = MorningSummary.builder()
                .tenant(tenant)
                .rep(rep)
                .driver(driver)
                .summaryDate(request.summaryDate())
                .summaryNumber(generateSummaryNumber(tenantId))
                .status("PENDING")
                .build();

        BigDecimal finalEstimateValue = BigDecimal.ZERO;

        if (items.isEmpty() && request.finalEstimateValue() != null) {
            finalEstimateValue = request.finalEstimateValue();
        }

        for (MorningSummaryRequest.MorningSummaryItemRequest itemRequest : items) {
            Product product = productRepository.findByIdAndTenantId(itemRequest.productId(), tenantId)
                    .orElseThrow(() -> new InvalidReferenceException("Product not found: " + itemRequest.productId()));

            BigDecimal unitPrice = product.getRatePerSoldUnit();
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) == 0) {
                unitPrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
            }
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
    @Transactional
    public MorningSummaryResponse updateMorningSummary(Long tenantId, Long summaryId, MorningSummaryRequest request) {
        List<MorningSummaryRequest.MorningSummaryItemRequest> items = request.items() != null ? request.items() : List.of();
        if (!items.isEmpty()) {
            List<Long> productIds = items.stream()
                    .map(MorningSummaryRequest.MorningSummaryItemRequest::productId)
                    .toList();
            java.util.Set<Long> uniqueProductIds = new java.util.HashSet<>(productIds);
            if (uniqueProductIds.size() != productIds.size()) {
                throw new BusinessRuleViolationException("Duplicate products are not allowed in a morning summary. Please merge quantities for the same product into a single line item.");
            }
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        MorningSummary morningSummary = morningSummaryRepository.findByIdAndTenantId(summaryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Morning Summary not found"));

        if (!"PENDING".equals(morningSummary.getStatus())) {
            throw new BusinessRuleViolationException("Only PENDING morning summaries can be updated.");
        }

        Rep rep = repRepository.findByIdAndTenantId(request.repId(), tenantId)
                .orElseThrow(() -> new InvalidReferenceException("Rep not found"));

        Driver driver = driverRepository.findByIdAndTenantId(request.driverId(), tenantId)
                .orElseThrow(() -> new InvalidReferenceException("Driver not found"));

        morningSummary.setRep(rep);
        morningSummary.setDriver(driver);
        morningSummary.setSummaryDate(request.summaryDate());

        morningSummary.getItems().clear();
        entityManager.flush();

        BigDecimal finalEstimateValue = BigDecimal.ZERO;

        if (items.isEmpty() && request.finalEstimateValue() != null) {
            finalEstimateValue = request.finalEstimateValue();
        }

        for (MorningSummaryRequest.MorningSummaryItemRequest itemRequest : items) {
            Product product = productRepository.findByIdAndTenantId(itemRequest.productId(), tenantId)
                    .orElseThrow(() -> new InvalidReferenceException("Product not found: " + itemRequest.productId()));

            BigDecimal unitPrice = product.getRatePerSoldUnit();
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) == 0) {
                unitPrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
            }
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
    public Page<MorningSummaryResponse> getAllSummaries(Long tenantId, java.time.LocalDate startDate, java.time.LocalDate endDate, Pageable pageable) {
        Page<MorningSummary> summaries;
        if (startDate != null && endDate != null) {
            summaries = morningSummaryRepository.findByTenantIdAndSummaryDateBetween(tenantId, startDate, endDate, pageable);
        } else if (startDate != null) {
            summaries = morningSummaryRepository.findByTenantIdAndSummaryDateBetween(tenantId, startDate, java.time.LocalDate.now(java.time.ZoneId.systemDefault()).plusYears(100), pageable);
        } else if (endDate != null) {
            summaries = morningSummaryRepository.findByTenantIdAndSummaryDateBetween(tenantId, java.time.LocalDate.of(1970, 1, 1), endDate, pageable);
        } else {
            summaries = morningSummaryRepository.findByTenantId(tenantId, pageable);
        }
        return summaries.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public MorningSummaryResponse getSummaryById(Long tenantId, Long summaryId) {
        MorningSummary summary = morningSummaryRepository.findByIdAndTenantId(summaryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Morning Summary not found"));
        return mapToResponse(summary);
    }

    private String generateSummaryNumber(Long tenantId) {
        return morningSummaryRepository.findLatestSummaryNumberByTenantId(tenantId)
                .map(lastNumber -> {
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
                .filter(item -> {
                    try {
                        // Must call getName() to trigger lazy initialization.
                        // getId() on a Hibernate proxy does NOT initialize it,
                        // so soft-deleted products would pass through undetected.
                        return item.getProduct() != null && item.getProduct().getName() != null;
                    } catch (jakarta.persistence.EntityNotFoundException e) {
                        return false; // Skip items whose product has been soft-deleted
                    }
                })
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
                .deductedWarehouseId(summary.getDeductedWarehouse() != null ? summary.getDeductedWarehouse().getId() : null)
                .deductedWarehouseName(summary.getDeductedWarehouse() != null ? summary.getDeductedWarehouse().getName() : null)
                .returnWarehouseId(summary.getReturnWarehouse() != null ? summary.getReturnWarehouse().getId() : null)
                .returnWarehouseName(summary.getReturnWarehouse() != null ? summary.getReturnWarehouse().getName() : null)
                .items(itemResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public DeductInventoryPreCheckResponse preCheckDeduction(Long tenantId, Long summaryId, Long warehouseId) {
        MorningSummary summary = morningSummaryRepository.findByIdAndTenantId(summaryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Morning Summary not found"));
        
        List<ItemAvailability> itemAvailabilities = new ArrayList<>();
        boolean canDeduct = true;

        for (MorningSummaryItem item : summary.getItems()) {
            try {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    continue; // Skip items with deleted products
                }
            } catch (jakarta.persistence.EntityNotFoundException e) {
                continue; // Skip items whose product has been soft-deleted
            }

            int required = item.getQuantity();
            int available = inventoryItemRepository
                    .findByProductIdAndWarehouseIdAndTenantId(item.getProduct().getId(), warehouseId, tenantId)
                    .map(inv -> inv.getQuantityAvailable() != null ? inv.getQuantityAvailable() : 0)
                    .orElse(0);

            boolean sufficient = available >= required;
            if (!sufficient) {
                canDeduct = false;
            }

            itemAvailabilities.add(new ItemAvailability(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    required,
                    available,
                    sufficient,
                    item.getExpectedReturnAmount() != null ? item.getExpectedReturnAmount() : 0
            ));
        }

        return new DeductInventoryPreCheckResponse(canDeduct, itemAvailabilities);
    }

    @Transactional
    @SuppressWarnings("NullAway")
    public void deductFromInventory(Long tenantId, Long summaryId, Long warehouseId, Long returnWarehouseId) {
        MorningSummary summary = morningSummaryRepository.findByIdAndTenantId(summaryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Morning Summary not found"));

        if (!"PENDING".equals(summary.getStatus())) {
            throw new BusinessRuleViolationException("Only PENDING morning summaries can be deducted");
        }

        Warehouse warehouse = warehouseRepository.findByIdAndTenantId(warehouseId, tenantId)
                .orElseThrow(() -> new InvalidReferenceException("Warehouse not found"));
                
        Warehouse returnWarehouse = warehouseRepository.findByIdAndTenantId(returnWarehouseId, tenantId)
                .orElseThrow(() -> new InvalidReferenceException("Return Warehouse not found"));

        DeductInventoryPreCheckResponse preCheck = preCheckDeduction(tenantId, summaryId, warehouseId);
        if (!preCheck.canDeduct()) {
            String missingItems = preCheck.items().stream()
                    .filter(i -> !i.sufficient())
                    .map(i -> i.productName() + " (req: " + i.requiredQuantity() + ", avail: " + i.availableQuantity() + ")")
                    .collect(Collectors.joining(", "));
            throw new BusinessRuleViolationException("Insufficient inventory for items: " + missingItems);
        }

        for (MorningSummaryItem item : summary.getItems()) {
            try {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    continue;
                }
            } catch (jakarta.persistence.EntityNotFoundException e) {
                continue;
            }

            if (item.getQuantity() > 0) {
                InventoryItem inventoryItem = inventoryItemRepository
                        .findByProductIdAndWarehouseIdAndTenantId(item.getProduct().getId(), warehouseId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

                inventoryItem.setQuantityAvailable(inventoryItem.getQuantityAvailable() - item.getQuantity());
                inventoryItemRepository.save(inventoryItem);

                inventoryLedgerService.recordMovement(
                        tenantId,
                        warehouseId,
                        item.getProduct().getId(),
                        InventoryMovementType.MORNING_DISPATCH,
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
                        .transactionType("MORNING_DISPATCH")
                        .quantity(-item.getQuantity())
                        .referenceId(summary.getSummaryNumber())
                        .notes("Deducted for morning summary")
                        .tenant(summary.getTenant())
                        .build();
                inventoryTransactionRepository.save(tx);
            }
            
            if (item.getExpectedReturnAmount() != null && item.getExpectedReturnAmount() > 0) {
                InventoryItem returnInventoryItem = inventoryItemRepository
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

                returnInventoryItem.setQuantityAvailable(returnInventoryItem.getQuantityAvailable() + item.getExpectedReturnAmount());
                inventoryItemRepository.save(returnInventoryItem);

                inventoryLedgerService.recordMovement(
                        tenantId,
                        returnWarehouseId,
                        item.getProduct().getId(),
                        InventoryMovementType.MORNING_RETURN_RECEIPT,
                        BigDecimal.valueOf(item.getExpectedReturnAmount()),
                        item.getUnitPrice(),
                        summary.getSummaryNumber(),
                        "",
                        null,
                        Instant.now(),
                        "system"
                );

                InventoryTransaction tx = InventoryTransaction.builder()
                        .inventoryItem(returnInventoryItem)
                        .transactionType("MORNING_RETURN_RECEIPT")
                        .quantity(item.getExpectedReturnAmount())
                        .referenceId(summary.getSummaryNumber())
                        .notes("Added return for morning summary")
                        .tenant(summary.getTenant())
                        .build();
                inventoryTransactionRepository.save(tx);
            }
        }

        summary.setStatus("SETTLED");
        summary.setDeductedWarehouse(warehouse);
        summary.setReturnWarehouse(returnWarehouse);
        morningSummaryRepository.save(summary);
    }

    @Transactional
    @SuppressWarnings("NullAway")
    public void undoDeduction(Long tenantId, Long summaryId) {
        MorningSummary summary = morningSummaryRepository.findByIdAndTenantId(summaryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Morning Summary not found"));

        if (!"SETTLED".equals(summary.getStatus())) {
            throw new BusinessRuleViolationException("Only SETTLED morning summaries can be undone");
        }

        if (summary.getDeductedWarehouse() == null) {
            throw new BusinessRuleViolationException("Cannot undo: deducted warehouse is unknown");
        }
        
        if (summary.getReturnWarehouse() == null) {
            throw new BusinessRuleViolationException("Cannot undo: return warehouse is unknown");
        }

        Long warehouseId = summary.getDeductedWarehouse().getId();
        Long returnWarehouseId = summary.getReturnWarehouse().getId();

        for (MorningSummaryItem item : summary.getItems()) {
            try {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    continue;
                }
            } catch (jakarta.persistence.EntityNotFoundException e) {
                continue;
            }

            if (item.getQuantity() > 0) {
                InventoryItem inventoryItem = inventoryItemRepository
                        .findByProductIdAndWarehouseIdAndTenantId(item.getProduct().getId(), warehouseId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

                inventoryItem.setQuantityAvailable(inventoryItem.getQuantityAvailable() + item.getQuantity());
                inventoryItemRepository.save(inventoryItem);

                inventoryLedgerService.recordMovement(
                        tenantId,
                        warehouseId,
                        item.getProduct().getId(),
                        InventoryMovementType.MORNING_DISPATCH_REVERSAL,
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
                        .transactionType("MORNING_DISPATCH_REVERSAL")
                        .quantity(item.getQuantity())
                        .referenceId(summary.getSummaryNumber() + "-REVERSAL")
                        .notes("Undone morning summary deduction")
                        .tenant(summary.getTenant())
                        .build();
                inventoryTransactionRepository.save(tx);
            }
            
            if (item.getExpectedReturnAmount() != null && item.getExpectedReturnAmount() > 0) {
                InventoryItem returnInventoryItem = inventoryItemRepository
                        .findByProductIdAndWarehouseIdAndTenantId(item.getProduct().getId(), returnWarehouseId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Return inventory item not found"));

                returnInventoryItem.setQuantityAvailable(returnInventoryItem.getQuantityAvailable() - item.getExpectedReturnAmount());
                inventoryItemRepository.save(returnInventoryItem);

                inventoryLedgerService.recordMovement(
                        tenantId,
                        returnWarehouseId,
                        item.getProduct().getId(),
                        InventoryMovementType.MORNING_RETURN_REVERSAL,
                        BigDecimal.valueOf(-item.getExpectedReturnAmount()),
                        item.getUnitPrice(),
                        summary.getSummaryNumber() + "-REVERSAL",
                        "",
                        null,
                        Instant.now(),
                        "system"
                );

                InventoryTransaction tx = InventoryTransaction.builder()
                        .inventoryItem(returnInventoryItem)
                        .transactionType("MORNING_RETURN_REVERSAL")
                        .quantity(-item.getExpectedReturnAmount())
                        .referenceId(summary.getSummaryNumber() + "-REVERSAL")
                        .notes("Undone morning summary return")
                        .tenant(summary.getTenant())
                        .build();
                inventoryTransactionRepository.save(tx);
            }
        }

        summary.setStatus("PENDING");
        summary.setDeductedWarehouse(null);
        summary.setReturnWarehouse(null);
        morningSummaryRepository.save(summary);
    }
}
