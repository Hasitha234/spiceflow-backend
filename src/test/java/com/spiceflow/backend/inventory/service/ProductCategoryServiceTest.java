package com.spiceflow.backend.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.ProductCategoryRequest;
import com.spiceflow.backend.inventory.dto.response.ProductCategoryResponse;
import com.spiceflow.backend.inventory.entity.ProductCategory;
import com.spiceflow.backend.inventory.mapper.ProductCategoryMapper;
import com.spiceflow.backend.inventory.repository.ProductCategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceTest {

    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private ProductCategoryMapper productCategoryMapper;

    @InjectMocks private ProductCategoryService productCategoryService;

    private Tenant tenant;
    private ProductCategory category;
    @Mock private ProductCategoryResponse response;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        category = new ProductCategory();
        category.setId(1L);
        category.setName("Category 1");
        category.setTenant(tenant);
    }

    @Test
    void createCategory_Success() {
        ProductCategoryRequest request = new ProductCategoryRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "Category 1");
        
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(category);
        when(productCategoryMapper.toResponse(category)).thenReturn(response);

        ProductCategoryResponse result = productCategoryService.createCategory(1L, request);

        assertNotNull(result);
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }

    @Test
    void createCategory_WithParent_Success() {
        ProductCategoryRequest request = new ProductCategoryRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "Category 2");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "parentCategoryId", 1L);

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(productCategoryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(category));
        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(category);
        when(productCategoryMapper.toResponse(category)).thenReturn(response);

        ProductCategoryResponse result = productCategoryService.createCategory(1L, request);

        assertNotNull(result);
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }

    @Test
    void getCategories_WithSearch() {
        Page<ProductCategory> page = new PageImpl<>(List.of(category));
        when(productCategoryRepository.searchByTenantId(eq(1L), eq("Cat"), any(PageRequest.class))).thenReturn(page);
        when(productCategoryMapper.toResponse(category)).thenReturn(response);

        Page<ProductCategoryResponse> result = productCategoryService.getCategories(1L, "Cat", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getCategories_WithoutSearch() {
        Page<ProductCategory> page = new PageImpl<>(List.of(category));
        when(productCategoryRepository.findByTenantId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(productCategoryMapper.toResponse(category)).thenReturn(response);

        Page<ProductCategoryResponse> result = productCategoryService.getCategories(1L, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getCategory_Success() {
        when(productCategoryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(category));
        when(productCategoryMapper.toResponse(category)).thenReturn(response);

        ProductCategoryResponse result = productCategoryService.getCategory(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getCategory_NotFound() {
        when(productCategoryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productCategoryService.getCategory(1L, 1L));
    }

    @Test
    void updateCategory_Success() {
        ProductCategoryRequest request = new ProductCategoryRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "Updated Category");

        when(productCategoryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(category));
        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(category);
        when(productCategoryMapper.toResponse(category)).thenReturn(response);

        ProductCategoryResponse result = productCategoryService.updateCategory(1L, 1L, request);

        assertNotNull(result);
        assertEquals("Updated Category", category.getName());
    }

    @Test
    void updateCategory_OwnParent_ThrowsException() {
        ProductCategoryRequest request = new ProductCategoryRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "Updated Category");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "parentCategoryId", 1L);

        when(productCategoryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(category));

        assertThrows(BusinessRuleViolationException.class, () -> productCategoryService.updateCategory(1L, 1L, request));
    }

    @Test
    void deleteCategory_Success() {
        when(productCategoryRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(category));

        productCategoryService.deleteCategory(1L, 1L);

        verify(productCategoryRepository).delete(category);
    }
}
