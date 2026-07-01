package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Optional<InventoryTransaction> findByIdAndTenantId(Long id, Long tenantId);
    List<InventoryTransaction> findByInventoryItemIdAndTenantIdOrderByCreatedAtDesc(Long inventoryItemId, Long tenantId);
}
