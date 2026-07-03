package com.spiceflow.backend.inventory.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.ProductRequest;
import com.spiceflow.backend.inventory.dto.response.ProductResponse;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.ProductCategory;
import com.spiceflow.backend.inventory.entity.Supplier;
import com.spiceflow.backend.inventory.repository.ProductRepository;
import com.spiceflow.backend.inventory.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;
    private final ProductCategoryService productCategoryService;
    private final SupplierService supplierService;
    private final ProductMapper productMapper;

    @Transactional(rollbackFor = Exception.class)
    public ProductResponse createProduct(Long tenantId, ProductRequest request) {
        log.debug("Creating product for tenantId: {}, SKU: {}", tenantId, request.sku());
        try {
            Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant with ID " + tenantId + " not found"));
            
            productRepository.findBySkuAndTenantId(request.sku(), tenantId)
                .ifPresent(p -> {
                    throw new BusinessRuleViolationException("Product with SKU already exists: " + request.sku());
                });
                
            ProductCategory category = productCategoryService.getCategoryEntity(request.categoryId(), tenantId);
            Supplier supplier = supplierService.getSupplierEntity(tenantId, request.supplierId());
            
            Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .basePrice(request.basePrice())
                .unitOfMeasure(request.unitOfMeasure())
                .netWeight(request.netWeight())
                .unitType(request.unitType())
                .boxConfiguration(request.boxConfiguration())
                .itemsPerSoldUnit(request.itemsPerSoldUnit())
                .soldUnitsPerBox(request.soldUnitsPerBox())
                .ratePerSoldUnit(request.ratePerSoldUnit())
                .category(category)
                .supplier(supplier)
                .tenant(tenant)
                .build();
                
            Product savedProduct = productRepository.save(product);
            log.info("Successfully created product with ID: {} for tenantId: {}", savedProduct.getId(), tenantId);
            return productMapper.toResponse(savedProduct);
        } catch (BusinessRuleViolationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create product for tenantId: {}, SKU: {}", tenantId, request.sku(), e);
            throw new BusinessRuleViolationException("Failed to create product: " + e.getMessage());
        }
    }
    
    public Page<ProductResponse> getProducts(Long tenantId, String search, Pageable pageable) {
        log.debug("Fetching products for tenantId: {}, search: {}", tenantId, search);
        try {
            Page<Product> productPage;
            if (search != null && !search.trim().isEmpty()) {
                productPage = productRepository.searchByTenantId(tenantId, search.trim(), pageable);
            } else {
                productPage = productRepository.findByTenantId(tenantId, pageable);
            }
            return productPage.map(productMapper::toResponse);
        } catch (Exception e) {
            log.error("Failed to fetch products for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to fetch products");
        }
    }
    
    public ProductResponse getProduct(Long id, Long tenantId) {
        log.debug("Fetching product with ID: {} for tenantId: {}", id, tenantId);
        return productMapper.toResponse(getProductEntity(id, tenantId));
    }
    
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse updateProduct(Long id, Long tenantId, ProductRequest request) {
        log.debug("Updating product with ID: {} for tenantId: {}", id, tenantId);
        try {
            Product product = getProductEntity(id, tenantId);
            
            if (!product.getSku().equals(request.sku())) {
                productRepository.findBySkuAndTenantId(request.sku(), tenantId)
                    .ifPresent(p -> {
                        throw new BusinessRuleViolationException("Product with SKU already exists: " + request.sku());
                    });
            }
            
            ProductCategory category = productCategoryService.getCategoryEntity(request.categoryId(), tenantId);
            Supplier supplier = supplierService.getSupplierEntity(tenantId, request.supplierId());
            
            product.setSku(request.sku());
            product.setName(request.name());
            product.setDescription(request.description());
            product.setBasePrice(request.basePrice());
            product.setUnitOfMeasure(request.unitOfMeasure());
            product.setNetWeight(request.netWeight());
            product.setUnitType(request.unitType());
            product.setBoxConfiguration(request.boxConfiguration());
            product.setItemsPerSoldUnit(request.itemsPerSoldUnit());
            product.setSoldUnitsPerBox(request.soldUnitsPerBox());
            product.setRatePerSoldUnit(request.ratePerSoldUnit());
            product.setCategory(category);
            product.setSupplier(supplier);
            
            Product updatedProduct = productRepository.save(product);
            log.info("Successfully updated product with ID: {} for tenantId: {}", id, tenantId);
            return productMapper.toResponse(updatedProduct);
        } catch (BusinessRuleViolationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update product with ID: {} for tenantId: {}", id, tenantId, e);
            throw new BusinessRuleViolationException("Failed to update product: " + e.getMessage());
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id, Long tenantId) {
        log.debug("Deleting product with ID: {} for tenantId: {}", id, tenantId);
        try {
            Product product = getProductEntity(id, tenantId);
            productRepository.delete(product);
            log.info("Successfully deleted product with ID: {} for tenantId: {}", id, tenantId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete product with ID: {} for tenantId: {}", id, tenantId, e);
            throw new BusinessRuleViolationException("Failed to delete product due to existing dependencies");
        }
    }
    
    public Product getProductEntity(Long id, Long tenantId) {
        return productRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> {
                log.error("Product not found with ID: {} for tenantId: {}", id, tenantId);
                return new ResourceNotFoundException("Product not found with id: " + id);
            });
    }
}
