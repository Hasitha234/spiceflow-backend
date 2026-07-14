package com.spiceflow.backend.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.ProductRequest;
import com.spiceflow.backend.inventory.dto.response.ProductResponse;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.ProductCategory;
import com.spiceflow.backend.inventory.entity.Supplier;
import com.spiceflow.backend.inventory.mapper.ProductMapper;
import com.spiceflow.backend.inventory.repository.ProductRepository;
import java.math.BigDecimal;
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
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private ProductCategoryService productCategoryService;
    @Mock private SupplierService supplierService;
    @Mock private ProductMapper productMapper;

    @InjectMocks private ProductService productService;

    private Tenant tenant;
    private ProductCategory category;
    private Supplier supplier;
    private Product product;
    @Mock private ProductResponse response;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        category = new ProductCategory();
        category.setId(1L);

        supplier = new Supplier();
        supplier.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setSku("SKU-123");
        product.setName("Test Product");
        product.setTenant(tenant);
        product.setCategory(category);
        product.setSupplier(supplier);
    }

    @Test
    void createProduct_Success() {
        ProductRequest request = ProductRequest.builder().name("Test Product").sku("SKU-123").unitOfMeasure("kg").categoryId(1L).supplierId(1L).build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(productRepository.findBySkuAndTenantId("SKU-123", 1L)).thenReturn(Optional.empty());
        when(productCategoryService.getCategoryEntity(1L, 1L)).thenReturn(category);
        when(supplierService.getSupplierEntity(1L, 1L)).thenReturn(supplier);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.createProduct(1L, request);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_SkuExists() {
        ProductRequest request = ProductRequest.builder().name("Test Product").sku("SKU-123").unitOfMeasure("kg").categoryId(1L).build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(productRepository.findBySkuAndTenantId("SKU-123", 1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessRuleViolationException.class, () -> productService.createProduct(1L, request));
    }

    @Test
    void getProducts_WithSearch() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByFilters(eq(1L), eq("SKU-123"), isNull(), isNull(), any(PageRequest.class))).thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(response);

        Page<ProductResponse> result = productService.getProducts(1L, "SKU-123", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getProducts_WithoutSearch() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByFilters(eq(1L), isNull(), isNull(), isNull(), any(PageRequest.class))).thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(response);

        Page<ProductResponse> result = productService.getProducts(1L, "", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getProducts_WithFilters() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByFilters(eq(1L), eq("SKU-123"), eq(2L), eq(3L), any(PageRequest.class))).thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(response);

        Page<ProductResponse> result = productService.getProducts(1L, "SKU-123", 2L, 3L, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getProduct_Success() {
        when(productRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getProduct(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void getProduct_NotFound() {
        when(productRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProduct(1L, 1L));
    }

    @Test
    void updateProduct_Success() {
        ProductRequest request = ProductRequest.builder().name("Updated Product").sku("SKU-456").unitOfMeasure("kg").categoryId(1L).supplierId(1L).build();

        when(productRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySkuAndTenantId("SKU-456", 1L)).thenReturn(Optional.empty());
        when(productCategoryService.getCategoryEntity(1L, 1L)).thenReturn(category);
        when(supplierService.getSupplierEntity(1L, 1L)).thenReturn(supplier);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.updateProduct(1L, 1L, request);

        assertNotNull(result);
        assertEquals("SKU-456", product.getSku());
    }

    @Test
    void updateProduct_SkuExists() {
        ProductRequest request = ProductRequest.builder().name("Test Product").sku("SKU-456").unitOfMeasure("kg").categoryId(1L).build();

        when(productRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySkuAndTenantId("SKU-456", 1L)).thenReturn(Optional.of(new Product()));

        assertThrows(BusinessRuleViolationException.class, () -> productService.updateProduct(1L, 1L, request));
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L, 1L);

        verify(productRepository).delete(product);
    }
}
