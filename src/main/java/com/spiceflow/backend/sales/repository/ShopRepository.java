package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByIdAndTenantId(Long id, Long tenantId);
    Page<Shop> findByTenantId(Long tenantId, Pageable pageable);
    Page<Shop> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name, Pageable pageable);
}
