package com.spiceflow.backend.receiving.adapter;

import com.spiceflow.backend.receiving.domain.GoodsReceipt;
import com.spiceflow.backend.receiving.domain.GoodsReceiptLine;
import com.spiceflow.backend.receiving.domain.GoodsReceiptState;
import com.spiceflow.backend.receiving.entity.GoodsReceiptEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class GoodsReceiptPersistenceAdapterTest {

    private GoodsReceiptPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GoodsReceiptPersistenceAdapter();
    }

    @Test
    void should_convert_domain_to_entity_and_back_without_loss() {
        GoodsReceipt gr = GoodsReceipt.create("GR-2026-0001", 10L, 100L, "PO-2026-0001", 50L, 5L, "admin@spiceflow.com");
        GoodsReceiptLine line1 = new GoodsReceiptLine(1L, 201L, new BigDecimal("100"), new BigDecimal("100"),
                new BigDecimal("95"), new BigDecimal("5"), "LOT-A", LocalDate.now().plusDays(365), new BigDecimal("10.00"));
        GoodsReceiptLine line2 = new GoodsReceiptLine(2L, 202L, new BigDecimal("50"), new BigDecimal("50"),
                new BigDecimal("50"), new BigDecimal("0"), "LOT-B", LocalDate.now().plusDays(180), new BigDecimal("20.00"));

        gr = new GoodsReceipt(gr, GoodsReceiptState.INSPECTING, List.of(line1, line2));

        // Convert to entity
        GoodsReceiptEntity entity = adapter.toEntity(gr);

        assertNotNull(entity);
        assertEquals("GR-2026-0001", entity.getReceiptNumber());
        assertEquals("PO-2026-0001", entity.getPoNumber());
        assertEquals(50L, entity.getSupplierId());
        assertEquals(10L, entity.getTenantId());
        assertEquals(GoodsReceiptState.INSPECTING, entity.getStatus());
        assertEquals(new BigDecimal("1950.00"), entity.getTotalAcceptedValue());
        assertEquals(new BigDecimal("50.00"), entity.getTotalDamagedValue());
        assertEquals(2, entity.getLines().size());

        // Convert back to domain
        GoodsReceipt restored = adapter.toDomain(entity);

        assertNotNull(restored);
        assertNotSame(gr, restored);
        assertEquals(gr.getReceiptNumber(), restored.getReceiptNumber());
        assertEquals(gr.getPoNumber(), restored.getPoNumber());
        assertEquals(gr.getSupplierId(), restored.getSupplierId());
        assertEquals(gr.getTenantId(), restored.getTenantId());
        assertEquals(gr.getState(), restored.getState());
        assertEquals(gr.getTotalAcceptedValue(), restored.getTotalAcceptedValue());
        assertEquals(gr.getTotalDamagedValue(), restored.getTotalDamagedValue());
        assertEquals(2, restored.getLines().size());
        assertEquals(new BigDecimal("950.00"), restored.getLines().get(0).getLineTotal());
        assertEquals(new BigDecimal("1000.00"), restored.getLines().get(1).getLineTotal());
    }

    @Test
    void should_preserve_immutability_during_mapping() {
        GoodsReceipt gr = GoodsReceipt.create("GR-2026-0002", 10L, 100L, "PO-2026-0002", 50L, 5L, "admin");
        GoodsReceiptLine line = new GoodsReceiptLine(1L, 301L, new BigDecimal("10"), new BigDecimal("10"),
                new BigDecimal("10"), new BigDecimal("0"), "LOT-X", null, new BigDecimal("50.00"));
        gr = new GoodsReceipt(gr, GoodsReceiptState.DRAFT, List.of(line));

        GoodsReceiptEntity entity = adapter.toEntity(gr);
        entity.getLines().clear();

        // Original domain aggregate must remain completely untouched
        assertEquals(1, gr.getLines().size());
    }
}
