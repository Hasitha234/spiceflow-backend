package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.DeliveryShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryShopRepository extends JpaRepository<DeliveryShop, Long> {
    Optional<DeliveryShop> findByIdAndTenantId(Long id, Long tenantId);
    Optional<DeliveryShop> findByDeliveryIdAndShopIdAndTenantId(Long deliveryId, Long shopId, Long tenantId);
}
