package com.spiceflow.backend.inventory.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.inventory.dto.request.ProductRequest;
import com.spiceflow.backend.inventory.dto.response.ProductResponse;
import com.spiceflow.backend.inventory.service.ProductService;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints for managing products (requires SETTINGS_PRODUCTS authority)")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('SETTINGS_PRODUCTS')")
    @Operation(summary = "Create a new product", description = "Creates a new product under the authenticated tenant's scope", operationId = "createProduct")
    public ResponseEntity<ProductResponse> createProduct(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ProductRequest request) {
        log.info("Received request to create product: {} by user: {}", request.name(), currentUser.getId());
        ProductResponse response = productService.createProduct(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "List all products", description = "Returns a paginated list of products, optionally filtered by search text, category, or supplier", operationId = "getProducts")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            Pageable pageable) {
        log.info("Received request to fetch products by user: {}", currentUser.getId());
        Page<ProductResponse> response = productService.getProducts(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), search, categoryId, supplierId, pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @Operation(summary = "Get product by ID", description = "Returns details of a specific product", operationId = "getProduct")
    public ResponseEntity<ProductResponse> getProduct(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("Received request to fetch product ID: {} by user: {}", id, currentUser.getId());
        ProductResponse response = productService.getProduct(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_PRODUCTS')")
    @Operation(summary = "Update product", description = "Updates details of an existing product", operationId = "updateProduct")
    public ResponseEntity<ProductResponse> updateProduct(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        log.info("Received request to update product ID: {} by user: {}", id, currentUser.getId());
        ProductResponse response = productService.updateProduct(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_PRODUCTS')")
    @Operation(summary = "Delete product", description = "Deletes a product if it has no associated inventory items", operationId = "deleteProduct")
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("Received request to delete product ID: {} by user: {}", id, currentUser.getId());
        productService.deleteProduct(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted product", description = "Restores a product that was previously soft-deleted", operationId = "restoreProduct")
    public ResponseEntity<ProductResponse> restoreProduct(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("Received request to restore product ID: {} by user: {}", id, currentUser.getId());
        ProductResponse response = productService.restoreProduct(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }
}
