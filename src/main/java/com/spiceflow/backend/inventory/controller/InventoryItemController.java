package com.spiceflow.backend.inventory.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.inventory.dto.request.InventoryItemRequest;
import com.spiceflow.backend.inventory.dto.response.InventoryItemResponse;
import com.spiceflow.backend.inventory.service.InventoryItemService;
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
@RequestMapping("/api/v1/inventory-items")
@RequiredArgsConstructor
@Tag(name = "Inventory Items", description = "Endpoints for managing warehouse inventory items (requires INVENTORY_VIEW/INVENTORY_TRANSFER authority)")
@PreAuthorize("hasAuthority('INVENTORY_VIEW')")
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Create an inventory item", description = "Registers a product in a warehouse with an initial quantity", operationId = "createInventoryItem")
    public ResponseEntity<InventoryItemResponse> createInventoryItem(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody InventoryItemRequest request) {
        log.info("Received request to create inventory item for product: {} at warehouse: {} by user: {}", 
            request.productId(), request.warehouseId(), currentUser.getId());
        InventoryItemResponse response = inventoryItemService.createInventoryItem(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all inventory items", description = "Returns a paginated list of inventory items, optionally filtered by warehouse and product", operationId = "getInventoryItems")
    public ResponseEntity<Page<InventoryItemResponse>> getInventoryItems(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            Pageable pageable) {
        log.info("Received request to fetch inventory items by user: {}", currentUser.getId());
        Page<InventoryItemResponse> response = inventoryItemService.getInventoryItems(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), warehouseId, productId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory item by ID", description = "Returns details of a specific inventory item", operationId = "getInventoryItem")
    public ResponseEntity<InventoryItemResponse> getInventoryItem(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("Received request to fetch inventory item ID: {} by user: {}", id, currentUser.getId());
        InventoryItemResponse response = inventoryItemService.getInventoryItem(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Update an inventory item", description = "Updates details (like batch number/expiration) of an existing inventory item", operationId = "updateInventoryItem")
    public ResponseEntity<InventoryItemResponse> updateInventoryItem(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id, @Valid @RequestBody InventoryItemRequest request) {
        log.info("Received request to update inventory item ID: {} by user: {}", id, currentUser.getId());
        InventoryItemResponse response = inventoryItemService.updateInventoryItem(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Delete an inventory item", description = "Deletes an inventory item only if quantities are zero", operationId = "deleteInventoryItem")
    public ResponseEntity<Void> deleteInventoryItem(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("Received request to delete inventory item ID: {} by user: {}", id, currentUser.getId());
        inventoryItemService.deleteInventoryItem(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Transfer inventory", description = "Transfer products between warehouses", operationId = "transferInventory")
    public ResponseEntity<Void> transferInventory(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody com.spiceflow.backend.inventory.dto.request.InventoryTransferRequest request) {
        log.info("Received request to transfer inventory by user: {}", currentUser.getId());
        inventoryItemService.transferInventory(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/mark-damaged")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Mark inventory damaged", description = "Mark products as damaged and deduct from available stock", operationId = "markDamaged")
    public ResponseEntity<Void> markDamaged(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody com.spiceflow.backend.inventory.dto.request.InventoryMarkDamagedRequest request) {
        log.info("Received request to mark inventory damaged by user: {}", currentUser.getId());
        inventoryItemService.markDamaged(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch-transfer")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Batch transfer inventory", description = "Transfer multiple products between warehouses", operationId = "batchTransferInventory")
    public ResponseEntity<com.spiceflow.backend.inventory.dto.response.InventoryBatchTransferResponse> batchTransfer(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody com.spiceflow.backend.inventory.dto.request.InventoryBatchTransferRequest request) {
        log.info("Received request to batch transfer inventory by user: {}", currentUser.getId());
        var response = inventoryItemService.batchTransfer(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.ok(response);
    }
}
