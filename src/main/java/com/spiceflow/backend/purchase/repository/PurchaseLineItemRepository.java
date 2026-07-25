package com.spiceflow.backend.purchase.repository;

import com.spiceflow.backend.purchase.entity.PurchaseLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseLineItemRepository extends JpaRepository<PurchaseLineItem, Long> {
    
    List<PurchaseLineItem> findByPurchaseIdAndTenantId(Long purchaseId, Long tenantId);
    
    void deleteByPurchaseIdAndTenantId(Long purchaseId, Long tenantId);

    boolean existsByProductIdAndTenantId(Long productId, Long tenantId);
}
