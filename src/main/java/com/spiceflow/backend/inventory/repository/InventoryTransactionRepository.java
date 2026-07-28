package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Optional<InventoryTransaction> findByIdAndTenantId(Long id, Long tenantId);
    List<InventoryTransaction> findByInventoryItemIdAndTenantIdOrderByCreatedAtDesc(Long inventoryItemId, Long tenantId);
    List<InventoryTransaction> findByReferenceIdAndTenantId(String referenceId, Long tenantId);
    
    Page<InventoryTransaction> findByTenantId(Long tenantId, Pageable pageable);
    
    Page<InventoryTransaction> findByInventoryItemIdAndTransactionTypeAndTenantId(Long inventoryItemId, String transactionType, Long tenantId, Pageable pageable);
    
    Page<InventoryTransaction> findByInventoryItemIdAndTenantId(Long inventoryItemId, Long tenantId, Pageable pageable);
    
    Page<InventoryTransaction> findByTransactionTypeAndTenantId(String transactionType, Long tenantId, Pageable pageable);
}
