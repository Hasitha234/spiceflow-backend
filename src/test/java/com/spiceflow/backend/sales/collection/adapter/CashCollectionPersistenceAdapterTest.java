package com.spiceflow.backend.sales.collection.adapter;

import com.spiceflow.backend.sales.collection.domain.CashCollection;
import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import com.spiceflow.backend.sales.collection.entity.CashCollectionWorkflowEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CashCollectionPersistenceAdapterTest {

    private CashCollectionPersistenceAdapter adapter;

    @BeforeEach
    void setup() {
        adapter = new CashCollectionPersistenceAdapter();
    }

    @Test
    void should_convert_domain_to_entity_with_full_fidelity() {
        CashCollection domain = CashCollection.create("COL-2026-0001", 10L, 100L, 5L,
                LocalDate.now(ZoneOffset.UTC), BigDecimal.valueOf(5000),
                "CHEQUE", "CHQ-123", "BOC", LocalDate.now(ZoneOffset.UTC), "Cheque payment", "rep1");

        CashCollectionWorkflowEntity entity = adapter.toEntity(domain);

        assertNotNull(entity);
        assertEquals("COL-2026-0001", entity.getCollectionNumber());
        assertEquals(CashCollectionState.PENDING, entity.getStatus());
        assertEquals(BigDecimal.valueOf(5000), entity.getAmount());
        assertEquals("CHEQUE", entity.getPaymentMethod());
        assertEquals("CHQ-123", entity.getChequeNo());
        assertEquals("BOC", entity.getChequeBankName());
    }

    @Test
    void should_restore_domain_from_entity_with_full_fidelity() {
        CashCollection domain = CashCollection.create("COL-2026-0001", 10L, 100L, 5L,
                LocalDate.now(ZoneOffset.UTC), BigDecimal.valueOf(5000),
                "CASH", null, null, null, "Cash payment", "rep1");

        CashCollectionWorkflowEntity entity = adapter.toEntity(domain);
        CashCollection restored = adapter.toAggregate(entity);

        assertNotNull(restored);
        assertEquals(domain.getCollectionNumber(), restored.getCollectionNumber());
        assertEquals(domain.getTenantId(), restored.getTenantId());
        assertEquals(domain.getShopId(), restored.getShopId());
        assertEquals(domain.getAmount(), restored.getAmount());
        assertEquals(domain.getState(), restored.getState());
    }
}
