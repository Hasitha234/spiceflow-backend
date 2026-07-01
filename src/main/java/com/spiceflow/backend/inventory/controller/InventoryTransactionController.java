package com.spiceflow.backend.inventory.controller;

import com.spiceflow.backend.auth.entity.User;
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
@RequestMapping("/api/v1/inventory-transactions")
@RequiredArgsConstructor
@Tag(name = "Inventory Transactions", description = "Endpoints for managing stock movements (requires INVENTORY_VIEW/INVENTORY_TRANSFER authority)")
@PreAuthorize("hasAuthority('INVENTORY_VIEW')")
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Create an inventory transaction", description = "Records a stock movement (IN, OUT, RESERVE, RELEASE) and updates inventory quantities")
    public ResponseEntity<InventoryTransactionResponse> createTransaction(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody InventoryTransactionRequest request) {
        log.info("Received request to create inventory transaction type: {} for item: {} by user: {}", 
            request.getTransactionType(), request.getInventoryItemId(), currentUser.getId());
        InventoryTransactionResponse response = transactionService.recordTransaction(currentUser.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all inventory transactions", description = "Returns an immutable paginated ledger of all stock movements")
    public ResponseEntity<Page<InventoryTransactionResponse>> getTransactions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Long inventoryItemId,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        log.info("Received request to fetch inventory transactions by user: {}", currentUser.getId());
        Page<InventoryTransactionResponse> response = transactionService.getTransactions(currentUser.getTenantId(), inventoryItemId, type, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory transaction by ID", description = "Returns details of a specific stock movement")
    public ResponseEntity<InventoryTransactionResponse> getTransaction(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("Received request to fetch inventory transaction ID: {} by user: {}", id, currentUser.getId());
        InventoryTransactionResponse response = transactionService.getTransaction(id, currentUser.getTenantId());
        return ResponseEntity.ok(response);
    }
}
