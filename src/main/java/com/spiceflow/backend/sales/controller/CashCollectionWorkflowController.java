package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.collection.domain.CashCollection;
import com.spiceflow.backend.sales.collection.domain.CashCollectionState;
import com.spiceflow.backend.sales.collection.dto.CashCollectionResponse;
import com.spiceflow.backend.sales.collection.dto.CreateCashCollectionRequest;
import com.spiceflow.backend.sales.collection.service.CashCollectionWorkflowService;
import com.spiceflow.backend.sales.collection.workflow.command.CancelCashCollectionCommand;
import com.spiceflow.backend.sales.collection.workflow.command.ConfirmCashCollectionCommand;
import com.spiceflow.backend.workflow.WorkflowContext;
import com.spiceflow.backend.workflow.WorkflowResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/sales/collections")
@RequiredArgsConstructor
@Tag(name = "Cash Collections", description = "Endpoints for managing cash collections and receivables accounting")
public class CashCollectionWorkflowController {

    private final CashCollectionWorkflowService workflowService;

    @PostMapping
    @PreAuthorize("hasAuthority('PAYMENT_CREATE') or hasAuthority('PAYMENT_WRITE')")
    @Operation(summary = "Create cash collection", description = "Creates a new cash collection in PENDING state", operationId = "createCashCollection")
    public ResponseEntity<CashCollectionResponse> createCollection(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateCashCollectionRequest request) {
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        log.info("User {} creating cash collection for shop {}", currentUser.getId(), request.shopId());

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
                currentUser.getUsername()
        );

        CashCollection created = workflowService.createCollection(collection);
        return ResponseEntity.status(HttpStatus.CREATED).body(CashCollectionResponse.from(created));
    }

    @PostMapping("/{collectionNumber}/confirm")
    @PreAuthorize("hasAuthority('PAYMENT_CREATE') or hasAuthority('PAYMENT_WRITE')")
    @Operation(summary = "Confirm cash collection", description = "Confirms collection and deducts amount from shop outstanding loan balance", operationId = "confirmCashCollection")
    public ResponseEntity<CashCollectionResponse> confirmCollection(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String collectionNumber,
            @RequestParam(required = false) String comment) {
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        log.info("User {} confirming cash collection {}", currentUser.getId(), collectionNumber);

        WorkflowContext context = new WorkflowContext(
                currentUser.getId() != null ? currentUser.getId() : 1L,
                tenantId,
                collectionNumber,
                Instant.now(Clock.systemUTC())
        );
        WorkflowResult<CashCollection> result = workflowService.executeCommand(
                collectionNumber, tenantId, new ConfirmCashCollectionCommand(comment), context
        );
        return ResponseEntity.ok(CashCollectionResponse.from(result.updatedAggregate()));
    }

    @PostMapping("/{collectionNumber}/cancel")
    @PreAuthorize("hasAuthority('PAYMENT_CREATE') or hasAuthority('PAYMENT_WRITE')")
    @Operation(summary = "Cancel cash collection", description = "Cancels collection and reverses balance deduction if previously confirmed", operationId = "cancelCashCollection")
    public ResponseEntity<CashCollectionResponse> cancelCollection(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String collectionNumber,
            @RequestParam(required = false) String comment) {
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        log.info("User {} cancelling cash collection {}", currentUser.getId(), collectionNumber);

        WorkflowContext context = new WorkflowContext(
                currentUser.getId() != null ? currentUser.getId() : 1L,
                tenantId,
                collectionNumber,
                Instant.now(Clock.systemUTC())
        );
        WorkflowResult<CashCollection> result = workflowService.executeCommand(
                collectionNumber, tenantId, new CancelCashCollectionCommand(comment), context
        );
        return ResponseEntity.ok(CashCollectionResponse.from(result.updatedAggregate()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYMENT_VIEW') or hasAuthority('PAYMENT_READ')")
    @Operation(summary = "List cash collections", description = "Returns all cash collections for the tenant with optional status filter", operationId = "listCashCollections")
    public ResponseEntity<List<CashCollectionResponse>> listCollections(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) CashCollectionState status) {
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        log.info("User {} listing cash collections with status {}", currentUser.getId(), status);

        List<CashCollection> collections = workflowService.listCollections(tenantId, status);
        List<CashCollectionResponse> response = collections.stream()
                .map(CashCollectionResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{collectionNumber}")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW') or hasAuthority('PAYMENT_READ')")
    @Operation(summary = "Get cash collection", description = "Returns details of a specific cash collection by number", operationId = "getCashCollection")
    public ResponseEntity<CashCollectionResponse> getCollection(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String collectionNumber) {
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        log.info("User {} getting cash collection {}", currentUser.getId(), collectionNumber);

        CashCollection collection = workflowService.getCollection(collectionNumber, tenantId);
        return ResponseEntity.ok(CashCollectionResponse.from(collection));
    }

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW') or hasAuthority('PAYMENT_READ')")
    @Operation(summary = "List collections by shop", description = "Returns all cash collections for a specific shop", operationId = "listCashCollectionsByShop")
    public ResponseEntity<List<CashCollectionResponse>> listCollectionsByShop(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long shopId) {
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        log.info("User {} listing cash collections for shop {}", currentUser.getId(), shopId);

        List<CashCollection> collections = workflowService.listCollectionsByShop(tenantId, shopId);
        List<CashCollectionResponse> response = collections.stream()
                .map(CashCollectionResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
