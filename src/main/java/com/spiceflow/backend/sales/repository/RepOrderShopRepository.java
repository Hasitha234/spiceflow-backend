package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.RepOrderShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepOrderShopRepository extends JpaRepository<RepOrderShop, Long> {
    Optional<RepOrderShop> findByIdAndTenantId(Long id, Long tenantId);
    List<RepOrderShop> findByRepOrderIdAndTenantId(Long repOrderId, Long tenantId);
}
