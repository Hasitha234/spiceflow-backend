package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.ProductCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByIdAndTenantId(Long id, Long tenantId);
}
