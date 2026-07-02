package com.spiceflow.backend.inventory.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.entity.User;
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
    @Operation(summary = "Create a new product category", description = "Creates a new category under the authenticated tenant's scope")
    public ResponseEntity<ProductCategoryResponse> createCategory(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ProductCategoryRequest request) {
        log.info("Received request to create product category: {} by user: {}", request.getName(), currentUser.getId());
        ProductCategoryResponse response = productCategoryService.createCategory(currentUser.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all product categories", description = "Returns a paginated list of categories, optionally filtered by search text")
    public ResponseEntity<Page<ProductCategoryResponse>> getCategories(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        log.info("Received request to fetch product categories by user: {}", currentUser.getId());
        Page<ProductCategoryResponse> response = productCategoryService.getCategories(currentUser.getTenantId(), search, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product category by ID", description = "Returns details of a specific category")
    public ResponseEntity<ProductCategoryResponse> getCategory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("Received request to fetch product category ID: {} by user: {}", id, currentUser.getId());
        ProductCategoryResponse response = productCategoryService.getCategory(id, currentUser.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product category", description = "Updates details of an existing category")
    public ResponseEntity<ProductCategoryResponse> updateCategory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id, @Valid @RequestBody ProductCategoryRequest request) {
        log.info("Received request to update product category ID: {} by user: {}", id, currentUser.getId());
        ProductCategoryResponse response = productCategoryService.updateCategory(id, currentUser.getTenantId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product category", description = "Deletes a category if it has no child categories or products")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("Received request to delete product category ID: {} by user: {}", id, currentUser.getId());
        productCategoryService.deleteCategory(id, currentUser.getTenantId());
        return ResponseEntity.noContent().build();
    }
}
