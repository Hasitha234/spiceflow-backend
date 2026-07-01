package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.ProductCategory;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByIdAndTenantId(Long id, Long tenantId);
    
    Page<ProductCategory> findByTenantId(Long tenantId, Pageable pageable);
    
    @Query("SELECT p FROM ProductCategory p WHERE p.tenant.id = :tenantId AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ProductCategory> searchByTenantId(@Param("tenantId") Long tenantId, 
                                           @Param("search") String search, 
                                           Pageable pageable);
}
