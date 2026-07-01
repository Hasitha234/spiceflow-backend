package com.spiceflow.backend.inventory.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.ProductCategoryRequest;
import com.spiceflow.backend.inventory.dto.response.ProductCategoryResponse;
import com.spiceflow.backend.inventory.entity.ProductCategory;
import com.spiceflow.backend.inventory.repository.ProductCategoryRepository;
import com.spiceflow.backend.inventory.mapper.ProductCategoryMapper;
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
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final TenantRepository tenantRepository;
    private final ProductCategoryMapper productCategoryMapper;

    @Transactional(rollbackFor = Exception.class)
    public ProductCategoryResponse createCategory(Long tenantId, ProductCategoryRequest request) {
        log.debug("Creating product category for tenantId: {}, name: {}", tenantId, request.getName());
        try {
            Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant with ID " + tenantId + " not found"));
            
            ProductCategory parentCategory = null;
            if (request.getParentCategoryId() != null) {
                parentCategory = getCategoryEntity(request.getParentCategoryId(), tenantId);
            }
            
            ProductCategory category = ProductCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentCategory(parentCategory)
                .tenant(tenant)
                .build();
                
            ProductCategory savedCategory = productCategoryRepository.save(category);
            log.info("Successfully created product category with ID: {} for tenantId: {}", savedCategory.getId(), tenantId);
            return productCategoryMapper.toResponse(savedCategory);
        } catch (Exception e) {
            log.error("Failed to create product category for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to create product category: " + e.getMessage());
        }
    }
    
    public Page<ProductCategoryResponse> getCategories(Long tenantId, String search, Pageable pageable) {
        log.debug("Fetching product categories for tenantId: {}, search: {}", tenantId, search);
        try {
            Page<ProductCategory> categoryPage;
            if (search != null && !search.trim().isEmpty()) {
                categoryPage = productCategoryRepository.searchByTenantId(tenantId, search.trim(), pageable);
            } else {
                categoryPage = productCategoryRepository.findByTenantId(tenantId, pageable);
            }
            return categoryPage.map(productCategoryMapper::toResponse);
        } catch (Exception e) {
            log.error("Failed to fetch product categories for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to fetch product categories");
        }
    }
    
    public ProductCategoryResponse getCategory(Long id, Long tenantId) {
        log.debug("Fetching product category with ID: {} for tenantId: {}", id, tenantId);
        return productCategoryMapper.toResponse(getCategoryEntity(id, tenantId));
    }
    
    @Transactional(rollbackFor = Exception.class)
    public ProductCategoryResponse updateCategory(Long id, Long tenantId, ProductCategoryRequest request) {
        log.debug("Updating product category with ID: {} for tenantId: {}", id, tenantId);
        try {
            ProductCategory category = getCategoryEntity(id, tenantId);
            
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            
            if (request.getParentCategoryId() != null) {
                if (request.getParentCategoryId().equals(id)) {
                    throw new BusinessRuleViolationException("A category cannot be its own parent");
                }
                ProductCategory parentCategory = getCategoryEntity(request.getParentCategoryId(), tenantId);
                category.setParentCategory(parentCategory);
            } else {
                category.setParentCategory(null);
            }
            
            ProductCategory updatedCategory = productCategoryRepository.save(category);
            log.info("Successfully updated product category with ID: {} for tenantId: {}", id, tenantId);
            return productCategoryMapper.toResponse(updatedCategory);
        } catch (BusinessRuleViolationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update product category with ID: {} for tenantId: {}", id, tenantId, e);
            throw new BusinessRuleViolationException("Failed to update product category: " + e.getMessage());
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id, Long tenantId) {
        log.debug("Deleting product category with ID: {} for tenantId: {}", id, tenantId);
        try {
            ProductCategory category = getCategoryEntity(id, tenantId);
            productCategoryRepository.delete(category);
            log.info("Successfully deleted product category with ID: {} for tenantId: {}", id, tenantId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete product category with ID: {} for tenantId: {}", id, tenantId, e);
            throw new BusinessRuleViolationException("Failed to delete product category due to existing dependencies");
        }
    }
    
    public ProductCategory getCategoryEntity(Long id, Long tenantId) {
        return productCategoryRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> {
                log.error("Product category not found with ID: {} for tenantId: {}", id, tenantId);
                return new ResourceNotFoundException("Product Category not found with id: " + id);
            });
    }
}
