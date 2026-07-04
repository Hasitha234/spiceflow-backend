package com.spiceflow.backend.sales.delivery.adapter;

import com.spiceflow.backend.sales.delivery.domain.Delivery;
import com.spiceflow.backend.sales.delivery.domain.DeliveryPaymentRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryReturnItemRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryShopItemRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryShopRecord;
import com.spiceflow.backend.sales.delivery.domain.DeliveryState;
import com.spiceflow.backend.sales.delivery.entity.DeliveryWorkflowEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeliveryPersistenceAdapterTest {

    private DeliveryPersistenceAdapter adapter;

    @BeforeEach
    void setup() {
        adapter = new DeliveryPersistenceAdapter();
    }

    @Test
    void should_convert_domain_to_entity_with_full_fidelity() {
        DeliveryShopItemRecord item = new DeliveryShopItemRecord(
                1L, 101L, 10, "PACK",
                BigDecimal.valueOf(500), BigDecimal.valueOf(5000),
                BigDecimal.ZERO, BigDecimal.valueOf(5000), false
        );
        DeliveryReturnItemRecord ret = new DeliveryReturnItemRecord(
                2L, 102L, 2, "PACK", BigDecimal.valueOf(200), "EXPIRED"
        );
        DeliveryPaymentRecord payment = new DeliveryPaymentRecord(
                3L, "CASH", BigDecimal.valueOf(4800), null, null, null
        );
        DeliveryShopRecord shop = new DeliveryShopRecord(
                10L, 201L,
                BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.valueOf(200),
                BigDecimal.valueOf(4800), BigDecimal.valueOf(4800), BigDecimal.ZERO,
                List.of(item), List.of(ret), List.of(payment)
        );

        Delivery domain = Delivery.create("DEL-2026-0001", 99L, 300L, "LS-2026-0001",
                LocalDate.now(), "admin", List.of(shop));

        DeliveryWorkflowEntity entity = adapter.toEntity(domain);

        assertNotNull(entity);
        assertEquals("DEL-2026-0001", entity.getDeliveryNumber());
        assertEquals(DeliveryState.IN_PROGRESS, entity.getStatus());
        assertEquals(1, entity.getShops().size());
        assertEquals(1, entity.getShops().get(0).getItems().size());
        assertEquals(1, entity.getShops().get(0).getReturns().size());
        assertEquals(1, entity.getShops().get(0).getPayments().size());
    }

    @Test
    void should_restore_domain_from_entity_with_full_fidelity() {
        DeliveryShopItemRecord item = new DeliveryShopItemRecord(
                1L, 101L, 10, "PACK",
                BigDecimal.valueOf(500), BigDecimal.valueOf(5000),
                BigDecimal.ZERO, BigDecimal.valueOf(5000), false
        );
        DeliveryReturnItemRecord ret = new DeliveryReturnItemRecord(
                2L, 102L, 2, "PACK", BigDecimal.valueOf(200), "DAMAGED"
        );
        DeliveryPaymentRecord payment = new DeliveryPaymentRecord(
                3L, "CHEQUE", BigDecimal.valueOf(4800), "CH-001", "Bank XYZ", LocalDate.now()
        );
        DeliveryShopRecord shop = new DeliveryShopRecord(
                10L, 201L,
                BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.valueOf(200),
                BigDecimal.valueOf(4800), BigDecimal.valueOf(4800), BigDecimal.ZERO,
                List.of(item), List.of(ret), List.of(payment)
        );

        Delivery domain = Delivery.create("DEL-2026-0002", 99L, 300L, "LS-2026-0002",
                LocalDate.now(), "admin", List.of(shop));

        DeliveryWorkflowEntity entity = adapter.toEntity(domain);
        Delivery restored = adapter.toDomain(entity);

        assertNotNull(restored);
        assertEquals(domain.getDeliveryNumber(), restored.getDeliveryNumber());
        assertEquals(domain.getState(), restored.getState());
        assertEquals(1, restored.getShops().size());

        DeliveryShopRecord restoredShop = restored.getShops().get(0);
        assertEquals(1, restoredShop.items().size());
        assertEquals(1, restoredShop.returns().size());
        assertEquals(1, restoredShop.payments().size());
        assertEquals(10, restoredShop.items().get(0).quantityDelivered());
        assertEquals("DAMAGED", restoredShop.returns().get(0).returnType());
        assertEquals("CHEQUE", restoredShop.payments().get(0).paymentMethod());
        assertEquals("CH-001", restoredShop.payments().get(0).chequeNo());
    }
}
