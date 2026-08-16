package com.spiceflow.backend.inventory.ledger.service;

import com.spiceflow.backend.inventory.ledger.InventoryLedgerEntry;
import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import com.spiceflow.backend.inventory.ledger.entity.InventoryLedgerEntryEntity;
import com.spiceflow.backend.inventory.ledger.repository.InventoryLedgerRepository;
import com.spiceflow.backend.receiving.domain.GoodsReceipt;
import com.spiceflow.backend.receiving.domain.GoodsReceiptLine;
import com.spiceflow.backend.receiving.domain.GoodsReceiptState;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spiceflow.backend.inventory.repository.ProductRepository;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;

@ExtendWith(MockitoExtension.class)
class InventoryLedgerServiceTest {

    @Mock
    private InventoryLedgerRepository repository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private InventoryLedgerService service;

    @BeforeEach
    void setUp() {
        service = new InventoryLedgerService(repository, productRepository, warehouseRepository);
    }

    @Test
    void should_record_movement_and_return_domain_entry() {
        InventoryLedgerEntryEntity savedEntity = new InventoryLedgerEntryEntity(
                10L, 5L, 201L, InventoryMovementType.RECEIPT,
                new BigDecimal("100"), new BigDecimal("10.00"), new BigDecimal("1000.00"),
                "GR-2026-0001", "LOT-A", null, Instant.now(), "admin"
        );
        when(repository.save(any(InventoryLedgerEntryEntity.class))).thenReturn(savedEntity);

        InventoryLedgerEntry entry = service.recordMovement(
                10L, 5L, 201L, InventoryMovementType.RECEIPT,
                new BigDecimal("100"), new BigDecimal("10.00"), "GR-2026-0001",
                "LOT-A", null, Instant.now(), "admin"
        );

        assertNotNull(entry);
        assertEquals(10L, entry.tenantId());
        assertEquals(5L, entry.warehouseId());
        assertEquals(201L, entry.productId());
        assertEquals(InventoryMovementType.RECEIPT, entry.movementType());
        assertEquals(new BigDecimal("1000.00"), entry.totalValue());
    }

    @Test
    void should_record_goods_receipt_accepted_quantities_only() {
        GoodsReceipt gr = GoodsReceipt.create("GR-2026-0001", 10L, 100L, "PO-2026-0001", 50L, 5L, "admin");
        GoodsReceiptLine line1 = new GoodsReceiptLine(1L, 201L, new BigDecimal("100"), new BigDecimal("100"),
                new BigDecimal("95"), new BigDecimal("5"), "LOT-A", LocalDate.now().plusDays(365), new BigDecimal("10.00"));
        GoodsReceiptLine line2 = new GoodsReceiptLine(2L, 202L, new BigDecimal("50"), new BigDecimal("50"),
                new BigDecimal("0"), new BigDecimal("50"), "LOT-B", null, new BigDecimal("20.00")); // 0 accepted, all damaged!

        gr = new GoodsReceipt(gr, GoodsReceiptState.POSTED, List.of(line1, line2));

        when(repository.save(any(InventoryLedgerEntryEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        List<InventoryLedgerEntry> entries = service.recordGoodsReceipt(gr);

        // Should only record line1 (95 accepted), line2 has 0 accepted so NO receipt ledger entry created!
        assertEquals(1, entries.size());
        assertEquals(new BigDecimal("95"), entries.get(0).quantity());
        assertEquals("LOT-A", entries.get(0).lotNumber());

        ArgumentCaptor<InventoryLedgerEntryEntity> captor = ArgumentCaptor.forClass(InventoryLedgerEntryEntity.class);
        verify(repository, times(1)).save(captor.capture());
        assertEquals(new BigDecimal("95"), captor.getValue().getQuantity());
        assertEquals(new BigDecimal("950.00"), captor.getValue().getTotalValue());
    }

    @Test
    void should_calculate_stock_balance_from_repository_sum() {
        when(repository.calculateStockBalance(10L, 5L, 201L)).thenReturn(new BigDecimal("250.00"));

        BigDecimal balance = service.getStockBalance(10L, 5L, 201L);

        assertEquals(new BigDecimal("250.00"), balance);
        verify(repository, times(1)).calculateStockBalance(10L, 5L, 201L);
    }
}
