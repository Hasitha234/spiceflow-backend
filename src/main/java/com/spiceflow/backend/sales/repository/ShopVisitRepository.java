package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.ShopVisit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopVisitRepository extends JpaRepository<ShopVisit, Long> {
    List<ShopVisit> findByDeliveryIdAndTenantId(Long deliveryId, Long tenantId);
    List<ShopVisit> findByShopIdAndTenantId(Long shopId, Long tenantId);
}
