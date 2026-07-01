package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Product> findBySkuAndTenantId(String sku, Long tenantId);
}
