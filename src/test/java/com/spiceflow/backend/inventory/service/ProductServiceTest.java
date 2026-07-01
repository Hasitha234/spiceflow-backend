package com.spiceflow.backend.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ProductCategoryService productCategoryService;

    @Mock
    private SupplierService supplierService;

    @Mock
    private com.spiceflow.backend.inventory.mapper.ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Tenant mockTenant;
    private ProductCategory mockCategory;
    private Supplier mockSupplier;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        mockTenant = new Tenant();
        mockTenant.setId(1L);

        mockCategory = ProductCategory.builder()
                .name("Spices")
                .tenant(mockTenant)
                .build();
        mockCategory.setId(10L);

        mockSupplier = new Supplier();
        mockSupplier.setId(20L);
        mockSupplier.setName("Supplier A");

        mockProduct = Product.builder()
                .name("Cinnamon")
                .sku("CIN-001")
                .basePrice(new BigDecimal("10.50"))
                .category(mockCategory)
                .supplier(mockSupplier)
                .tenant(mockTenant)
                .build();
        mockProduct.setId(100L);
    }

    @Test
    void testGetProducts_WithoutSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(mockProduct));

        when(productRepository.findByTenantId(1L, pageable)).thenReturn(page);

        ProductResponse mockResponse = ProductResponse.builder().id(100L).name("Cinnamon").build();
        when(productMapper.toResponse(any(Product.class))).thenReturn(mockResponse);

        Page<ProductResponse> result = productService.getProducts(1L, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Cinnamon", result.getContent().get(0).getName());
        verify(productRepository).findByTenantId(1L, pageable);
    }

    @Test
    void testCreateProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setName("Paprika");
        request.setSku("PAP-001");
        request.setBasePrice(new BigDecimal("12.00"));
        request.setCategoryId(10L);
        request.setSupplierId(20L);

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(productCategoryService.getCategoryEntity(10L, 1L)).thenReturn(mockCategory);
        when(supplierService.getSupplierEntity(1L, 20L)).thenReturn(mockSupplier);
        when(productRepository.findBySkuAndTenantId("PAP-001", 1L)).thenReturn(Optional.empty());

        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(101L);
            return p;
        });

        ProductResponse mockResponse = ProductResponse.builder().id(101L).name("Paprika").build();
        when(productMapper.toResponse(any(Product.class))).thenReturn(mockResponse);

        ProductResponse response = productService.createProduct(1L, request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Paprika", response.getName());
    }

    @Test
    void testCreateProduct_DuplicateSku() {
        ProductRequest request = new ProductRequest();
        request.setName("Paprika");
        request.setSku("CIN-001"); // Existing SKU
        request.setBasePrice(new BigDecimal("12.00"));
        request.setCategoryId(10L);
        request.setSupplierId(20L);

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
        when(productRepository.findBySkuAndTenantId("CIN-001", 1L)).thenReturn(Optional.of(mockProduct));

        assertThrows(BusinessRuleViolationException.class, () -> {
            productService.createProduct(1L, request);
        });
    }
}
