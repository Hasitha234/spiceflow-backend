package com.spiceflow.backend.sales.loading.adapter;

import com.spiceflow.backend.sales.loading.domain.LoadingSheet;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetItem;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetReturnItem;
import com.spiceflow.backend.sales.loading.domain.LoadingSheetState;
import com.spiceflow.backend.sales.loading.entity.LoadingSheetWorkflowEntity;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoadingSheetPersistenceAdapterTest {

    private LoadingSheetPersistenceAdapter adapter;

    @BeforeEach
    void setup() {
        adapter = new LoadingSheetPersistenceAdapter();
    }

    @Test
    void should_convert_domain_to_entity_and_back() {
        LoadingSheetItem item = new LoadingSheetItem(1L, 100L, 50, "PACK");
        LoadingSheetReturnItem ret = new LoadingSheetReturnItem(2L, 200L, 5, "BOX", "DAMAGED");

        LoadingSheet domain = LoadingSheet.create(
                "LS-2026-0001",
                1L,
                10L,
                "RO-2026-0001",
                5L,
                "John Driver",
                LocalDate.now(),
                "admin",
                List.of(item),
                List.of(ret)
        );

        LoadingSheetWorkflowEntity entity = adapter.toEntity(domain);
        assertNotNull(entity);
        assertEquals("LS-2026-0001", entity.getSheetNumber());
        assertEquals(LoadingSheetState.DRAFT, entity.getStatus());
        assertEquals(1, entity.getItems().size());
        assertEquals(1, entity.getReturns().size());

        LoadingSheet restored = adapter.toDomain(entity);
        assertNotNull(restored);
        assertEquals(domain.getSheetNumber(), restored.getSheetNumber());
        assertEquals(domain.getState(), restored.getState());
        assertEquals(1, restored.getItems().size());
        assertEquals(1, restored.getReturns().size());
        assertEquals(50, restored.getItems().get(0).quantityLoaded());
        assertEquals("DAMAGED", restored.getReturns().get(0).returnType());
    }
}
