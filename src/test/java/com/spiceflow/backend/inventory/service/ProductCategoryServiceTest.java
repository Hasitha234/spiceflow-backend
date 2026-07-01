package com.spiceflow.backend.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.inventory.dto.request.ProductCategoryRequest;
import com.spiceflow.backend.inventory.dto.response.ProductCategoryResponse;
import com.spiceflow.backend.inventory.entity.ProductCategory;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class ProductCategoryServiceTest {

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private com.spiceflow.backend.inventory.mapper.ProductCategoryMapper productCategoryMapper;

    @InjectMocks
    private ProductCategoryService categoryService;

    private Tenant mockTenant;
    private ProductCategory mockCategory;

    @BeforeEach
    void setUp() {
        mockTenant = new Tenant();
        mockTenant.setId(1L);

        mockCategory = ProductCategory.builder()
                .name("Spices")
                .description("All kinds of spices")
                .tenant(mockTenant)
                .build();
        mockCategory.setId(10L);
    }

    @Test
    void testGetCategories_WithoutSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductCategory> page = new PageImpl<>(List.of(mockCategory));

        when(categoryRepository.findByTenantId(1L, pageable)).thenReturn(page);

        ProductCategoryResponse mockResponse = ProductCategoryResponse.builder().id(10L).name("Spices").build();
        when(productCategoryMapper.toResponse(any(ProductCategory.class))).thenReturn(mockResponse);

        Page<ProductCategoryResponse> result = categoryService.getCategories(1L, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Spices", result.getContent().get(0).getName());
        verify(categoryRepository).findByTenantId(1L, pageable);
    }

    @Test
    void testCreateCategory_Success() {
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("Herbs");

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(categoryRepository.save(any(ProductCategory.class))).thenAnswer(i -> {
            ProductCategory c = i.getArgument(0);
            c.setId(20L);
            return c;
        });

        ProductCategoryResponse mockResponse = ProductCategoryResponse.builder().id(20L).name("Herbs").build();
        when(productCategoryMapper.toResponse(any(ProductCategory.class))).thenReturn(mockResponse);

        ProductCategoryResponse response = categoryService.createCategory(1L, request);

        assertNotNull(response);
        assertEquals(20L, response.getId());
        assertEquals("Herbs", response.getName());
    }
}
