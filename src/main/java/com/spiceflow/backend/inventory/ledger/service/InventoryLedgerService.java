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

@Service
public class InventoryLedgerService {

    private final InventoryLedgerRepository repository;

    public InventoryLedgerService(InventoryLedgerRepository repository) {
        this.repository = repository;
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
}
