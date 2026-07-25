package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Product> findBySkuAndTenantId(String sku, Long tenantId);
    Page<Product> findByTenantId(Long tenantId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.tenant.id = :tenantId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchByTenantId(@Param("tenantId") Long tenantId, 
                                   @Nullable @Param("search") String search, 
                                   Pageable pageable);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN p.supplier s " +
           "LEFT JOIN p.category c " +
           "WHERE p.tenant.id = :tenantId AND " +
           "(:supplierId IS NULL OR s.id = :supplierId) AND " +
           "(:categoryId IS NULL OR c.id = :categoryId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<Product> findByFilters(@Param("tenantId") Long tenantId,
                                @Nullable @Param("search") String search,
                                @Nullable @Param("categoryId") Long categoryId,
                                @Nullable @Param("supplierId") Long supplierId,
                                Pageable pageable);

    @Query(value = "SELECT * FROM products WHERE id = :id AND tenant_id = :tenantId AND deleted_at IS NOT NULL", nativeQuery = true)
    Optional<Product> findSoftDeletedByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}

