package com.spiceflow.backend.inventory.transfer.adapter;

import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransfer;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferLine;
import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
import com.spiceflow.backend.inventory.transfer.entity.WarehouseTransferEntity;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class WarehouseTransferPersistenceAdapterTest {

    private WarehouseTransferPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WarehouseTransferPersistenceAdapter();
    }

    @Test
    void should_convert_domain_to_entity_and_back_without_loss() {
        WarehouseTransfer wt = WarehouseTransfer.create("WT-2026-0001", 10L, 1L, 2L, "admin@spiceflow.com");
        WarehouseTransferLine line1 = new WarehouseTransferLine(1L, 201L, new BigDecimal("100"), new BigDecimal("100"),
                new BigDecimal("98"), new BigDecimal("2"), "LOT-A", new BigDecimal("10.00"));
        WarehouseTransferLine line2 = new WarehouseTransferLine(2L, 202L, new BigDecimal("50"), new BigDecimal("50"),
                new BigDecimal("50"), new BigDecimal("0"), "LOT-B", new BigDecimal("20.00"));

        wt = new WarehouseTransfer(wt, WarehouseTransferState.RECEIVED, List.of(line1, line2));

        // Convert to entity
        WarehouseTransferEntity entity = adapter.toEntity(wt);

        assertNotNull(entity);
        assertEquals("WT-2026-0001", entity.getTransferNumber());
        assertEquals(1L, entity.getFromWarehouseId());
        assertEquals(2L, entity.getToWarehouseId());
        assertEquals(10L, entity.getTenantId());
        assertEquals(WarehouseTransferState.RECEIVED, entity.getStatus());
        assertEquals(new BigDecimal("2000.00"), entity.getTotalTransferValue());
        assertEquals(2, entity.getLines().size());

        // Convert back to domain
        WarehouseTransfer restored = adapter.toDomain(entity);

        assertNotNull(restored);
        assertNotSame(wt, restored);
        assertEquals(wt.getTransferNumber(), restored.getTransferNumber());
        assertEquals(wt.getFromWarehouseId(), restored.getFromWarehouseId());
        assertEquals(wt.getToWarehouseId(), restored.getToWarehouseId());
        assertEquals(wt.getTenantId(), restored.getTenantId());
        assertEquals(wt.getState(), restored.getState());
        assertEquals(wt.getTotalTransferValue(), restored.getTotalTransferValue());
        assertEquals(2, restored.getLines().size());
        assertEquals(new BigDecimal("1000.00"), restored.getLines().get(0).getLineTotal());
        assertEquals(new BigDecimal("1000.00"), restored.getLines().get(1).getLineTotal());
    }

    @Test
    void should_preserve_immutability_during_mapping() {
        WarehouseTransfer wt = WarehouseTransfer.create("WT-2026-0002", 10L, 1L, 2L, "admin");
        WarehouseTransferLine line = new WarehouseTransferLine(1L, 301L, new BigDecimal("10"), new BigDecimal("0"),
                new BigDecimal("0"), new BigDecimal("0"), "LOT-X", new BigDecimal("50.00"));
        wt = new WarehouseTransfer(wt, WarehouseTransferState.DRAFT, List.of(line));

        WarehouseTransferEntity entity = adapter.toEntity(wt);
        entity.getLines().clear();

        // Original domain aggregate must remain completely untouched
        assertEquals(1, wt.getLines().size());
    }
}
