package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.ShopReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopReturnRepository extends JpaRepository<ShopReturn, Long> {
    Optional<ShopReturn> findByIdAndTenantId(Long id, Long tenantId);
    List<ShopReturn> findByRepOrderShopIdAndTenantId(Long repOrderShopId, Long tenantId);
}
