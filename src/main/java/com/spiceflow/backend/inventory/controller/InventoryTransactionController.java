package com.spiceflow.backend.inventory.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.inventory.dto.request.InventoryTransactionRequest;
import com.spiceflow.backend.inventory.dto.response.InventoryTransactionResponse;
import com.spiceflow.backend.inventory.service.InventoryTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/inventory-transactions")
@RequiredArgsConstructor
@Tag(name = "Inventory Transactions", description = "Endpoints for managing stock movements (requires INVENTORY_VIEW/INVENTORY_TRANSFER authority)")
@PreAuthorize("hasAuthority('INVENTORY_VIEW')")
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Create an inventory transaction", description = "Records a stock movement (IN, OUT, RESERVE, RELEASE) and updates inventory quantities", operationId = "createTransaction")
    public ResponseEntity<InventoryTransactionResponse> createTransaction(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody InventoryTransactionRequest request) {
        log.info("Received request to create inventory transaction type: {} for item: {} by user: {}", 
            request.transactionType(), request.inventoryItemId(), currentUser.getId());
        InventoryTransactionResponse response = transactionService.recordTransaction(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all inventory transactions", description = "Returns an immutable paginated ledger of all stock movements", operationId = "getTransactions")
    public ResponseEntity<Page<InventoryTransactionResponse>> getTransactions(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) Long inventoryItemId,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        log.info("Received request to fetch inventory transactions by user: {}", currentUser.getId());
        Page<InventoryTransactionResponse> response = transactionService.getTransactions(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), inventoryItemId, type, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory transaction by ID", description = "Returns details of a specific stock movement", operationId = "getTransaction")
    public ResponseEntity<InventoryTransactionResponse> getTransaction(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("Received request to fetch inventory transaction ID: {} by user: {}", id, currentUser.getId());
        InventoryTransactionResponse response = transactionService.getTransaction(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }
}
