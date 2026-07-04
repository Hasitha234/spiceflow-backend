package com.spiceflow.backend.inventory.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.inventory.dto.request.ProductCategoryRequest;
import com.spiceflow.backend.inventory.dto.response.ProductCategoryResponse;
import com.spiceflow.backend.inventory.service.ProductCategoryService;
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
@RequestMapping("/api/v1/product-categories")
@RequiredArgsConstructor
@Tag(name = "Product Categories", description = "Endpoints for managing product categories (requires SETTINGS_PRODUCTS authority)")
@PreAuthorize("hasAuthority('SETTINGS_PRODUCTS')")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @PostMapping
    @Operation(summary = "Create a new product category", description = "Creates a new category under the authenticated tenant's scope", operationId = "createCategory")
    public ResponseEntity<ProductCategoryResponse> createCategory(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ProductCategoryRequest request) {
        log.info("Received request to create product category: {} by user: {}", request.name(), currentUser.getId());
        ProductCategoryResponse response = productCategoryService.createCategory(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all product categories", description = "Returns a paginated list of categories, optionally filtered by search text", operationId = "getCategories")
    public ResponseEntity<Page<ProductCategoryResponse>> getCategories(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        log.info("Received request to fetch product categories by user: {}", currentUser.getId());
        Page<ProductCategoryResponse> response = productCategoryService.getCategories(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), search, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product category by ID", description = "Returns details of a specific category", operationId = "getCategory")
    public ResponseEntity<ProductCategoryResponse> getCategory(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("Received request to fetch product category ID: {} by user: {}", id, currentUser.getId());
        ProductCategoryResponse response = productCategoryService.getCategory(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product category", description = "Updates details of an existing category", operationId = "updateCategory")
    public ResponseEntity<ProductCategoryResponse> updateCategory(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id, @Valid @RequestBody ProductCategoryRequest request) {
        log.info("Received request to update product category ID: {} by user: {}", id, currentUser.getId());
        ProductCategoryResponse response = productCategoryService.updateCategory(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product category", description = "Deletes a category if it has no child categories or products", operationId = "deleteCategory")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("Received request to delete product category ID: {} by user: {}", id, currentUser.getId());
        productCategoryService.deleteCategory(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.noContent().build();
    }
}
