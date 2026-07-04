package com.spiceflow.backend.purchasing.adapter;

import com.spiceflow.backend.purchasing.domain.PurchaseOrder;
import com.spiceflow.backend.purchasing.domain.PurchaseOrderLine;
import com.spiceflow.backend.purchasing.domain.PurchaseOrderState;
import com.spiceflow.backend.purchasing.entity.PurchaseOrderEntity;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class PurchaseOrderPersistenceAdapterTest {

    private PurchaseOrderPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PurchaseOrderPersistenceAdapter();
    }

    @Test
    void should_convert_domain_to_entity_and_back_without_loss() {
        PurchaseOrder po = PurchaseOrder.create(100L, "PO-2026-0001", 10L, "admin@spiceflow.com");
        PurchaseOrderLine line1 = new PurchaseOrderLine(1L, 201L, new BigDecimal("10.00"), new BigDecimal("15.50"));
        PurchaseOrderLine line2 = new PurchaseOrderLine(2L, 202L, new BigDecimal("5.00"), new BigDecimal("20.00"));
        
        po = new PurchaseOrder(po, PurchaseOrderState.DRAFT, List.of(line1, line2));

        // Convert to entity
        PurchaseOrderEntity entity = adapter.toEntity(po);

        assertNotNull(entity);
        assertEquals("PO-2026-0001", entity.getCorrelationId());
        assertEquals(100L, entity.getSupplierId());
        assertEquals(10L, entity.getTenantId());
        assertEquals(PurchaseOrderState.DRAFT, entity.getStatus());
        assertEquals(new BigDecimal("255.00"), entity.getTotalAmount());
        assertEquals(2, entity.getLines().size());

        // Convert back to domain
        PurchaseOrder restored = adapter.toDomain(entity);

        assertNotNull(restored);
        assertNotSame(po, restored);
        assertEquals(po.getPoNumber(), restored.getPoNumber());
        assertEquals(po.getCorrelationId(), restored.getCorrelationId());
        assertEquals(po.getSupplierId(), restored.getSupplierId());
        assertEquals(po.getTenantId(), restored.getTenantId());
        assertEquals(po.getState(), restored.getState());
        assertEquals(po.getTotalAmount(), restored.getTotalAmount());
        assertEquals(2, restored.getLines().size());
        assertEquals(new BigDecimal("155.00"), restored.getLines().get(0).getLineTotal());
        assertEquals(new BigDecimal("100.00"), restored.getLines().get(1).getLineTotal());
    }

    @Test
    void should_preserve_immutability_during_mapping() {
        PurchaseOrder po = PurchaseOrder.create(100L, "PO-2026-0002");
        PurchaseOrderLine line = new PurchaseOrderLine(1L, 301L, new BigDecimal("1.00"), new BigDecimal("50.00"));
        po = new PurchaseOrder(po, PurchaseOrderState.DRAFT, List.of(line));

        PurchaseOrderEntity entity = adapter.toEntity(po);
        entity.getLines().clear();

        // Original domain aggregate must remain completely untouched
        assertEquals(1, po.getLines().size());
    }
}
