package com.spiceflow.backend.inventory.ledger.service;

import com.spiceflow.backend.inventory.ledger.InventoryLedgerEntry;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.entity.InventoryLedgerEntryEntity;
import com.spiceflow.backend.inventory.ledger.repository.InventoryLedgerRepository;
import com.spiceflow.backend.receiving.domain.GoodsReceipt;
import com.spiceflow.backend.receiving.domain.GoodsReceiptLine;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.spiceflow.backend.inventory.repository.ProductRepository;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.dto.response.TransferHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryLedgerService {

    private final InventoryLedgerRepository repository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryLedgerService(InventoryLedgerRepository repository,
                                  ProductRepository productRepository,
                                  WarehouseRepository warehouseRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public InventoryLedgerEntry recordMovement(Long tenantId, Long warehouseId, Long productId,
                                               InventoryMovementType movementType, BigDecimal quantity,
                                               BigDecimal unitCost, String referenceId, String lotNumber,
                                               @Nullable LocalDate expirationDate, Instant timestamp,
                                               String performedBy) {
        BigDecimal totalValue = quantity.multiply(unitCost);
        InventoryLedgerEntryEntity entity = new InventoryLedgerEntryEntity(
                tenantId, warehouseId, productId, movementType, quantity,
                unitCost, totalValue, referenceId, lotNumber, expirationDate,
                timestamp, performedBy
        );
        InventoryLedgerEntryEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Transactional
    public List<InventoryLedgerEntry> recordGoodsReceipt(GoodsReceipt gr) {
        List<InventoryLedgerEntry> entries = new ArrayList<>();
        Instant timestamp = gr.getPostedAt() != null ? gr.getPostedAt() : Instant.now();
        String performedBy = gr.getPostedBy() != null ? gr.getPostedBy() : "system";

        for (GoodsReceiptLine line : gr.getLines()) {
            if (line.getAcceptedQty().compareTo(BigDecimal.ZERO) > 0) {
                InventoryLedgerEntry entry = recordMovement(
                        gr.getTenantId(),
                        gr.getWarehouseId(),
                        line.getProductId(),
                        InventoryMovementType.RECEIPT,
                        line.getAcceptedQty(),
                        line.getUnitPrice(),
                        gr.getReceiptNumber(),
                        line.getLotNumber(),
                        line.getExpirationDate(),
                        timestamp,
                        performedBy
                );
                entries.add(entry);
            }
        }
        return entries;
    }

    @Transactional(readOnly = true)
    public BigDecimal getStockBalance(Long tenantId, Long warehouseId, Long productId) {
        return repository.calculateStockBalance(tenantId, warehouseId, productId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalStockBalance(Long tenantId, Long productId) {
        return repository.calculateTotalStockBalance(tenantId, productId);
    }

    @Transactional(readOnly = true)
    public List<InventoryLedgerEntry> getLedgerEntries(Long tenantId, Long warehouseId, Long productId) {
        return repository.findByTenantIdAndWarehouseIdAndProductId(tenantId, warehouseId, productId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private InventoryLedgerEntry toDomain(InventoryLedgerEntryEntity entity) {
        return new InventoryLedgerEntry(
                entity.getId(),
                entity.getTenantId(),
                entity.getWarehouseId(),
                entity.getProductId(),
                entity.getMovementType(),
                entity.getQuantity(),
                entity.getUnitCost(),
                entity.getTotalValue(),
                entity.getReferenceId(),
                entity.getLotNumber(),
                entity.getExpirationDate(),
                entity.getTimestamp(),
                entity.getPerformedBy()
        );
    }
    @Transactional(readOnly = true)
    public Page<TransferHistoryResponse> listTransfers(Long tenantId, Long warehouseId, Instant startDate, Instant endDate, Pageable pageable) {
        Page<InventoryLedgerEntryEntity> page = repository.findTransfers(
                tenantId,
                List.of(InventoryMovementType.TRANSFER_IN, InventoryMovementType.TRANSFER_OUT),
                warehouseId,
                startDate,
                endDate,
                pageable
        );

        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> productIds = page.getContent().stream().map(InventoryLedgerEntryEntity::getProductId).distinct().toList();
        List<Long> warehouseIds = page.getContent().stream().map(InventoryLedgerEntryEntity::getWarehouseId).distinct().toList();

        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, Warehouse> warehouses = warehouseRepository.findAllById(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, w -> w));

        return page.map(entity -> {
            Product p = products.get(entity.getProductId());
            Warehouse w = warehouses.get(entity.getWarehouseId());
            return new TransferHistoryResponse(
                    entity.getId() != null ? entity.getId() : 0L,
                    entity.getWarehouseId(),
                    w != null ? w.getName() : "Unknown",
                    entity.getProductId(),
                    p != null ? p.getName() : "Unknown",
                    p != null ? p.getSku() : "",
                    entity.getMovementType(),
                    entity.getQuantity(),
                    entity.getReferenceId(),
                    entity.getPerformedBy(),
                    entity.getTimestamp()
            );
        });
    }
}
