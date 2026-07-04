package com.spiceflow.backend.sales.collection.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.events.DomainEventPublisher;
import com.spiceflow.backend.sales.collection.adapter.CashCollectionPersistenceAdapter;
import com.spiceflow.backend.sales.collection.domain.CashCollection;
import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import com.spiceflow.backend.sales.collection.entity.CashCollectionWorkflowEntity;
import com.spiceflow.backend.sales.collection.repository.CashCollectionWorkflowRepository;
import com.spiceflow.backend.sales.collection.workflow.command.CancelCashCollectionCommand;
import com.spiceflow.backend.sales.collection.workflow.command.ConfirmCashCollectionCommand;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.repository.ShopRepository;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashCollectionWorkflowServiceTest {

    @Mock private CashCollectionWorkflowRepository repository;
    @Mock private CashCollectionPersistenceAdapter adapter;
    private WorkflowEngine engine = new WorkflowEngine();
    @Mock private DomainEventPublisher eventPublisher;
    @Mock private AuditService auditService;
    @Mock private ShopRepository shopRepository;

    private CashCollectionWorkflowService service;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        service = new CashCollectionWorkflowService(
                repository, adapter, engine, eventPublisher, auditService, shopRepository
        );
        context = new WorkflowContext(1L, 10L, "COL-2026-0001", Instant.now());
    }

    @Test
    void should_create_collection_successfully() {
        CashCollection collection = CashCollection.create("COL-2026-0001", 10L, 100L, 5L,
                LocalDate.now(ZoneOffset.UTC), BigDecimal.valueOf(5000), "CASH", null, null, null, "Note", "rep1");
        CashCollectionWorkflowEntity entity = new CashCollectionWorkflowEntity();
        when(adapter.toEntity(collection)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(adapter.toAggregate(entity)).thenReturn(collection);

        CashCollection created = service.createCollection(collection);

        assertNotNull(created);
        verify(repository).save(entity);
    }

    @Test
    void should_confirm_collection_and_reduce_shop_balance() {
        CashCollection pending = CashCollection.create("COL-2026-0001", 10L, 100L, 5L,
                LocalDate.now(ZoneOffset.UTC), BigDecimal.valueOf(5000), "CASH", null, null, null, "Note", "rep1");
        CashCollectionWorkflowEntity entity = new CashCollectionWorkflowEntity();

        Shop shop = new Shop();
        shop.setId(100L);
        shop.setOutstandingLoan(BigDecimal.valueOf(15000));

        when(repository.findByCollectionNumberAndTenantId("COL-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toAggregate(entity)).thenReturn(pending);
        when(shopRepository.findByIdAndTenantId(100L, 10L)).thenReturn(Optional.of(shop));
        when(adapter.toEntity(any(CashCollection.class))).thenReturn(entity);

        WorkflowResult<CashCollection> result = service.executeCommand(
                "COL-2026-0001", 10L, new ConfirmCashCollectionCommand("Verified"), context);

        assertEquals(CashCollectionState.CONFIRMED, result.updatedAggregate().getState());
        assertEquals(BigDecimal.valueOf(10000), shop.getOutstandingLoan());
        verify(shopRepository).save(shop);
        verify(auditService).record(any());
        verify(eventPublisher).publish(any());
    }

    @Test
    void should_reject_confirmation_when_amount_exceeds_shop_balance() {
        CashCollection pending = CashCollection.create("COL-2026-0001", 10L, 100L, 5L,
                LocalDate.now(ZoneOffset.UTC), BigDecimal.valueOf(20000), "CASH", null, null, null, "Note", "rep1");
        CashCollectionWorkflowEntity entity = new CashCollectionWorkflowEntity();

        Shop shop = new Shop();
        shop.setId(100L);
        shop.setOutstandingLoan(BigDecimal.valueOf(15000));

        when(repository.findByCollectionNumberAndTenantId("COL-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toAggregate(entity)).thenReturn(pending);
        when(shopRepository.findByIdAndTenantId(100L, 10L)).thenReturn(Optional.of(shop));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.executeCommand("COL-2026-0001", 10L, new ConfirmCashCollectionCommand("Verified"), context));

        assertEquals(BigDecimal.valueOf(15000), shop.getOutstandingLoan());
        verify(shopRepository, never()).save(any());
    }

    @Test
    void should_reverse_shop_balance_when_confirmed_collection_is_cancelled() {
        CashCollection confirmed = new CashCollection(
                CashCollection.create("COL-2026-0001", 10L, 100L, 5L,
                        LocalDate.now(ZoneOffset.UTC), BigDecimal.valueOf(5000), "CASH", null, null, null, "Note", "rep1"),
                CashCollectionState.CONFIRMED
        );
        CashCollectionWorkflowEntity entity = new CashCollectionWorkflowEntity();

        Shop shop = new Shop();
        shop.setId(100L);
        shop.setOutstandingLoan(BigDecimal.valueOf(10000));

        when(repository.findByCollectionNumberAndTenantId("COL-2026-0001", 10L)).thenReturn(Optional.of(entity));
        when(adapter.toAggregate(entity)).thenReturn(confirmed);
        when(shopRepository.findByIdAndTenantId(100L, 10L)).thenReturn(Optional.of(shop));
        when(adapter.toEntity(any(CashCollection.class))).thenReturn(entity);

        WorkflowResult<CashCollection> result = service.executeCommand(
                "COL-2026-0001", 10L, new CancelCashCollectionCommand("Bounced"), context);

        assertEquals(CashCollectionState.CANCELLED, result.updatedAggregate().getState());
        assertEquals(BigDecimal.valueOf(15000), shop.getOutstandingLoan());
        verify(shopRepository).save(shop);
    }
}
