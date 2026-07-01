package com.spiceflow.backend.inventory.controller;

import com.spiceflow.backend.auth.entity.User;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints for managing products (requires SETTINGS_PRODUCTS authority)")
@PreAuthorize("hasAuthority('SETTINGS_PRODUCTS')")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product under the authenticated tenant's scope")
    public ResponseEntity<ProductResponse> createProduct(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ProductRequest request) {
        log.info("Received request to create product: {} by user: {}", request.getName(), currentUser.getId());
        ProductResponse response = productService.createProduct(currentUser.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all products", description = "Returns a paginated list of products, optionally filtered by search text")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        log.info("Received request to fetch products by user: {}", currentUser.getId());
        Page<ProductResponse> response = productService.getProducts(currentUser.getTenantId(), search, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns details of a specific product")
    public ResponseEntity<ProductResponse> getProduct(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("Received request to fetch product ID: {} by user: {}", id, currentUser.getId());
        ProductResponse response = productService.getProduct(id, currentUser.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates details of an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        log.info("Received request to update product ID: {} by user: {}", id, currentUser.getId());
        ProductResponse response = productService.updateProduct(id, currentUser.getTenantId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Deletes a product if it has no associated inventory items")
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("Received request to delete product ID: {} by user: {}", id, currentUser.getId());
        productService.deleteProduct(id, currentUser.getTenantId());
        return ResponseEntity.noContent().build();
    }
}
