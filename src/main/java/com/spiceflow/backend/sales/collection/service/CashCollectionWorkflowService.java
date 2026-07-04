package com.spiceflow.backend.sales.collection.service;

import com.spiceflow.backend.audit.AuditService;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.events.DomainEvent;
import com.spiceflow.backend.events.DomainEventPublisher;
import com.spiceflow.backend.sales.collection.adapter.CashCollectionPersistenceAdapter;
import com.spiceflow.backend.sales.collection.domain.CashCollection;
import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import com.spiceflow.backend.sales.collection.dto.CashCollectionResponse;
import com.spiceflow.backend.sales.collection.dto.CreateCashCollectionRequest;
import com.spiceflow.backend.sales.collection.entity.CashCollectionWorkflowEntity;
import com.spiceflow.backend.sales.collection.repository.CashCollectionWorkflowRepository;
import com.spiceflow.backend.sales.collection.workflow.command.CancelCashCollectionCommand;
import com.spiceflow.backend.sales.collection.workflow.command.ConfirmCashCollectionCommand;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.repository.ShopRepository;
import com.spiceflow.backend.workflow.WorkflowCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowEngine;
import com.spiceflow.backend.workflow.WorkflowResult;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operational workflow service for Cash Collection.
 *
 * Orchestrates FSM transitions via {@link WorkflowEngine}, event publishing,
 * audit logging, and automated side-effects:
 *
 * <ul>
 *   <li><b>On CONFIRMED</b>: Validates payment against shop's outstanding loan balance.
 *       Prevents overpayment and deducts collection amount from outstanding loan.</li>
 *   <li><b>On CANCELLED (Reversal)</b>: If cancelling a previously CONFIRMED collection,
 *       re-adds the collection amount back to the shop's outstanding loan balance.</li>
 * </ul>
 */
@Slf4j
@Service
public class CashCollectionWorkflowService {

    private final CashCollectionWorkflowRepository repository;
    private final CashCollectionPersistenceAdapter adapter;
    private final WorkflowEngine engine;
    private final DomainEventPublisher eventPublisher;
    private final AuditService auditService;
    private final ShopRepository shopRepository;

    public CashCollectionWorkflowService(CashCollectionWorkflowRepository repository,
                                         CashCollectionPersistenceAdapter adapter,
                                         WorkflowEngine engine,
                                         DomainEventPublisher eventPublisher,
                                         AuditService auditService,
                                         ShopRepository shopRepository) {
        this.repository = repository;
        this.adapter = adapter;
        this.engine = engine;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.shopRepository = shopRepository;
    }

    @Transactional
    public CashCollection createCollection(CashCollection collection) {
        CashCollectionWorkflowEntity entity = adapter.toEntity(collection);
        CashCollectionWorkflowEntity saved = repository.save(entity);
        log.info("Created cash collection {} for tenant {}", collection.getCollectionNumber(), collection.getTenantId());
        return adapter.toAggregate(saved);
    }

    @Transactional
    public CashCollectionResponse createCollection(Long tenantId, CreateCashCollectionRequest request, String username) {
        String collectionNumber = "COL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CashCollection collection = CashCollection.create(
                collectionNumber,
                tenantId,
                request.shopId(),
                request.repId(),
                request.collectionDate(),
                request.amount(),
                request.paymentMethod(),
                request.chequeNo(),
                request.chequeBankName(),
                request.chequeDate(),
                request.notes(),
                username
        );
        CashCollection created = createCollection(collection);
        return CashCollectionResponse.from(created);
    }

    @Transactional
    public CashCollectionResponse confirmCollection(String collectionNumber, Long tenantId, Long userId, @Nullable String comment) {
        WorkflowContext context = new WorkflowContext(
                userId != null ? userId : 1L,
                tenantId,
                collectionNumber,
                Instant.now(Clock.systemUTC())
        );
        WorkflowResult<CashCollection> result = executeCommand(
                collectionNumber, tenantId, new ConfirmCashCollectionCommand(comment), context
        );
        return CashCollectionResponse.from(result.updatedAggregate());
    }

    @Transactional
    public CashCollectionResponse cancelCollection(String collectionNumber, Long tenantId, Long userId, @Nullable String comment) {
        WorkflowContext context = new WorkflowContext(
                userId != null ? userId : 1L,
                tenantId,
                collectionNumber,
                Instant.now(Clock.systemUTC())
        );
        WorkflowResult<CashCollection> result = executeCommand(
                collectionNumber, tenantId, new CancelCashCollectionCommand(comment), context
        );
        return CashCollectionResponse.from(result.updatedAggregate());
    }

    @Transactional(readOnly = true)
    public CashCollection getCollection(String collectionNumber, Long tenantId) {
        CashCollectionWorkflowEntity entity = repository.findByCollectionNumberAndTenantId(collectionNumber, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cash collection not found: " + collectionNumber));
        return adapter.toAggregate(entity);
    }

    @Transactional(readOnly = true)
    public CashCollection getCollectionById(Long id, Long tenantId) {
        CashCollectionWorkflowEntity entity = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cash collection not found with id: " + id));
        return adapter.toAggregate(entity);
    }

    @Transactional(readOnly = true)
    public List<CashCollection> listCollections(Long tenantId, @Nullable CashCollectionState status) {
        if (status == null) {
            return listAllCollections(tenantId);
        }
        return repository.findAllByTenantId(tenantId).stream()
                .filter(e -> e.getStatus() == status)
                .map(adapter::toAggregate)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CashCollection> listCollectionsByShop(Long tenantId, Long shopId) {
        return repository.findAllByTenantId(tenantId).stream()
                .filter(e -> e.getShopId().equals(shopId))
                .map(adapter::toAggregate)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CashCollection> listAllCollections(Long tenantId) {
        return repository.findAllByTenantId(tenantId).stream()
                .map(adapter::toAggregate)
                .toList();
    }

    @Transactional
    public WorkflowResult<CashCollection> executeCommand(String collectionNumber,
                                                         Long tenantId,
                                                         WorkflowCommand<CashCollection, CashCollectionState> command,
                                                         WorkflowContext context) {
        CashCollection current = getCollection(collectionNumber, tenantId);
        WorkflowResult<CashCollection> result = engine.execute(command, current, context);

        CashCollection updated = result.updatedAggregate();
        CashCollectionState newState = updated.getState();

        if (newState == CashCollectionState.CONFIRMED) {
            applyCollectionToShopBalance(updated, tenantId);
        }

        if (newState == CashCollectionState.CANCELLED && current.getState() == CashCollectionState.CONFIRMED) {
            reverseCollectionFromShopBalance(updated, tenantId);
        }

        CashCollectionWorkflowEntity updatedEntity = adapter.toEntity(updated);
        repository.save(updatedEntity);

        if (result.auditEntry() != null) {
            auditService.record(result.auditEntry());
        }

        for (DomainEvent event : result.events()) {
            eventPublisher.publish(event);
        }

        log.info("Cash collection {} transitioned to {} for tenant {}", collectionNumber, newState, tenantId);
        return result;
    }

    private void applyCollectionToShopBalance(CashCollection collection, Long tenantId) {
        Shop shop = shopRepository.findByIdAndTenantId(collection.getShopId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + collection.getShopId()));

        if (collection.getAmount().compareTo(shop.getOutstandingLoan()) > 0) {
            throw new BusinessRuleViolationException(
                    "Payment amount (" + collection.getAmount() + ") exceeds shop outstanding balance (" + shop.getOutstandingLoan() + ")"
            );
        }

        shop.setOutstandingLoan(shop.getOutstandingLoan().subtract(collection.getAmount()));
        shopRepository.save(shop);
        log.debug("Applied payment {} to shop {} balance", collection.getAmount(), collection.getShopId());
    }

    private void reverseCollectionFromShopBalance(CashCollection collection, Long tenantId) {
        Shop shop = shopRepository.findByIdAndTenantId(collection.getShopId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + collection.getShopId()));

        shop.setOutstandingLoan(shop.getOutstandingLoan().add(collection.getAmount()));
        shopRepository.save(shop);
        log.debug("Reversed payment {} from shop {} balance", collection.getAmount(), collection.getShopId());
    }
}
