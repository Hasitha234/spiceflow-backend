package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.InventoryItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByIdAndTenantId(Long id, Long tenantId);
    Optional<InventoryItem> findByProductIdAndWarehouseIdAndTenantId(Long productId, Long warehouseId, Long tenantId);
}
