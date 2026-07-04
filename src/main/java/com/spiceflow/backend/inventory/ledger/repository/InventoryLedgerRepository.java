package com.spiceflow.backend.inventory.ledger.repository;

import com.spiceflow.backend.inventory.ledger.entity.InventoryLedgerEntryEntity;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLedgerRepository extends JpaRepository<InventoryLedgerEntryEntity, Long> {

    List<InventoryLedgerEntryEntity> findByTenantIdAndWarehouseIdAndProductId(
            Long tenantId, Long warehouseId, Long productId
    );

    List<InventoryLedgerEntryEntity> findByReferenceId(String referenceId);

    @Query("SELECT COALESCE(SUM(e.quantity), 0) FROM InventoryLedgerEntryEntity e WHERE e.tenantId = :tenantId AND e.warehouseId = :warehouseId AND e.productId = :productId")
    BigDecimal calculateStockBalance(@Param("tenantId") Long tenantId,
                                     @Param("warehouseId") Long warehouseId,
                                     @Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(e.quantity), 0) FROM InventoryLedgerEntryEntity e WHERE e.tenantId = :tenantId AND e.productId = :productId")
    BigDecimal calculateTotalStockBalance(@Param("tenantId") Long tenantId,
                                          @Param("productId") Long productId);
}
