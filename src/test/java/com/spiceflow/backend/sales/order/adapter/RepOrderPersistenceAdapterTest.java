package com.spiceflow.backend.sales.order.adapter;

import com.spiceflow.backend.sales.order.domain.RepOrder;
import com.spiceflow.backend.sales.order.domain.RepOrderItem;
import com.spiceflow.backend.sales.order.domain.RepOrderShop;
import com.spiceflow.backend.sales.order.domain.RepOrderState;
import com.spiceflow.backend.sales.order.domain.ShopReturnItem;
import com.spiceflow.backend.sales.order.entity.RepOrderWorkflowEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class RepOrderPersistenceAdapterTest {

    private RepOrderPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RepOrderPersistenceAdapter();
    }

    @Test
    void should_convert_domain_to_entity_and_back_without_loss() {
        RepOrder ro = RepOrder.create("RO-2026-0001", 10L, 5L, LocalDate.now(), "South Route", "admin@spiceflow.com");
        RepOrderItem item1 = new RepOrderItem(1L, 101L, 10, "BOX", new BigDecimal("100.00"), new BigDecimal("1000.00"),
                BigDecimal.ZERO, new BigDecimal("1000.00"), false, 1);
        RepOrderItem item2 = new RepOrderItem(2L, 102L, 5, "PACK", new BigDecimal("50.00"), new BigDecimal("250.00"),
                BigDecimal.ZERO, new BigDecimal("250.00"), false, 1);
        ShopReturnItem return1 = new ShopReturnItem(1L, 201L, 2, "BOX", new BigDecimal("100.00"), "EXPIRED", "PENDING");

        RepOrderShop shop = new RepOrderShop(1L, 501L, new BigDecimal("1250.00"), new BigDecimal("100.00"),
                new BigDecimal("1150.00"), List.of(item1, item2), List.of(return1));

        ro = new RepOrder(ro, RepOrderState.APPROVED, List.of(shop));

        // Convert to entity
        RepOrderWorkflowEntity entity = adapter.toEntity(ro);

        assertNotNull(entity);
        assertEquals("RO-2026-0001", entity.getOrderNumber());
        assertEquals(5L, entity.getRepId());
        assertEquals(10L, entity.getTenantId());
        assertEquals(RepOrderState.APPROVED, entity.getStatus());
        assertEquals(new BigDecimal("1250.00"), entity.getTotalGrossAmount());
        assertEquals(new BigDecimal("100.00"), entity.getTotalReturnsValue());
        assertEquals(new BigDecimal("1150.00"), entity.getNetAmount());
        assertEquals(1, entity.getShops().size());
        assertEquals(2, entity.getShops().get(0).getItems().size());
        assertEquals(1, entity.getShops().get(0).getReturns().size());

        // Convert back to domain
        RepOrder restored = adapter.toDomain(entity);

        assertNotNull(restored);
        assertNotSame(ro, restored);
        assertEquals(ro.getOrderNumber(), restored.getOrderNumber());
        assertEquals(ro.getRepId(), restored.getRepId());
        assertEquals(ro.getTenantId(), restored.getTenantId());
        assertEquals(ro.getState(), restored.getState());
        assertEquals(ro.getTotalGrossAmount(), restored.getTotalGrossAmount());
        assertEquals(ro.getTotalReturnsValue(), restored.getTotalReturnsValue());
        assertEquals(ro.getNetAmount(), restored.getNetAmount());
        assertEquals(1, restored.getShops().size());
        assertEquals(2, restored.getShops().get(0).items().size());
        assertEquals(1, restored.getShops().get(0).returns().size());
    }

    @Test
    void should_preserve_immutability_during_mapping() {
        RepOrder ro = RepOrder.create("RO-2026-0002", 10L, 5L, LocalDate.now(), "West Route", "admin");
        RepOrderItem item = new RepOrderItem(1L, 301L, 1, "BOX", new BigDecimal("50.00"), new BigDecimal("50.00"),
                BigDecimal.ZERO, new BigDecimal("50.00"), false, 1);
        RepOrderShop shop = new RepOrderShop(1L, 502L, new BigDecimal("50.00"), BigDecimal.ZERO,
                new BigDecimal("50.00"), List.of(item), List.of());
        ro = new RepOrder(ro, RepOrderState.DRAFT, List.of(shop));

        RepOrderWorkflowEntity entity = adapter.toEntity(ro);
        entity.getShops().clear();

        // Original domain aggregate must remain completely untouched
        assertEquals(1, ro.getShops().size());
    }
}
