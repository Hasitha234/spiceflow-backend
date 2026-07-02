package com.spiceflow.backend.inventory.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.entity.User;
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
    @Operation(summary = "Create an inventory item", description = "Registers a product in a warehouse with an initial quantity")
    public ResponseEntity<InventoryItemResponse> createInventoryItem(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody InventoryItemRequest request) {
        log.info("Received request to create inventory item for product: {} at warehouse: {} by user: {}", 
            request.getProductId(), request.getWarehouseId(), currentUser.getId());
        InventoryItemResponse response = inventoryItemService.createInventoryItem(currentUser.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all inventory items", description = "Returns a paginated list of inventory items, optionally filtered by warehouse and product")
    public ResponseEntity<Page<InventoryItemResponse>> getInventoryItems(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            Pageable pageable) {
        log.info("Received request to fetch inventory items by user: {}", currentUser.getId());
        Page<InventoryItemResponse> response = inventoryItemService.getInventoryItems(currentUser.getTenantId(), warehouseId, productId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory item by ID", description = "Returns details of a specific inventory item")
    public ResponseEntity<InventoryItemResponse> getInventoryItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("Received request to fetch inventory item ID: {} by user: {}", id, currentUser.getId());
        InventoryItemResponse response = inventoryItemService.getInventoryItem(id, currentUser.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Update an inventory item", description = "Updates details (like batch number/expiration) of an existing inventory item")
    public ResponseEntity<InventoryItemResponse> updateInventoryItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id, @Valid @RequestBody InventoryItemRequest request) {
        log.info("Received request to update inventory item ID: {} by user: {}", id, currentUser.getId());
        InventoryItemResponse response = inventoryItemService.updateInventoryItem(id, currentUser.getTenantId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Delete an inventory item", description = "Deletes an inventory item only if quantities are zero")
    public ResponseEntity<Void> deleteInventoryItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("Received request to delete inventory item ID: {} by user: {}", id, currentUser.getId());
        inventoryItemService.deleteInventoryItem(id, currentUser.getTenantId());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Transfer inventory", description = "Transfer products between warehouses")
    public ResponseEntity<Void> transferInventory(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody com.spiceflow.backend.inventory.dto.request.InventoryTransferRequest request) {
        log.info("Received request to transfer inventory by user: {}", currentUser.getId());
        inventoryItemService.transferInventory(currentUser.getTenantId(), request);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/mark-damaged")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSFER')")
    @Operation(summary = "Mark inventory damaged", description = "Mark products as damaged and deduct from available stock")
    public ResponseEntity<Void> markDamaged(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody com.spiceflow.backend.inventory.dto.request.InventoryMarkDamagedRequest request) {
        log.info("Received request to mark inventory damaged by user: {}", currentUser.getId());
        inventoryItemService.markDamaged(currentUser.getTenantId(), request);
        return ResponseEntity.ok().build();
    }
}
