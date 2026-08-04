package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.InventoryItem;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByIdAndTenantId(Long id, Long tenantId);
    Optional<InventoryItem> findByProductIdAndWarehouseIdAndTenantId(Long productId, Long warehouseId, Long tenantId);
    
    Page<InventoryItem> findByTenantId(Long tenantId, Pageable pageable);
    
    Page<InventoryItem> findByWarehouseIdAndProductIdAndTenantId(Long warehouseId, Long productId, Long tenantId, Pageable pageable);
    
    Page<InventoryItem> findByWarehouseIdAndTenantId(Long warehouseId, Long tenantId, Pageable pageable);
    
    Page<InventoryItem> findByProductIdAndTenantId(Long productId, Long tenantId, Pageable pageable);
    
    boolean existsByProductIdAndTenantId(Long productId, Long tenantId);

    @Query("SELECT new com.spiceflow.backend.sales.dto.response.StockStatusResponse(" +
           "p.id, p.name, p.sku, " +
           "CAST(COALESCE(SUM(CASE WHEN w.storeType = 'MAIN' THEN i.quantityAvailable ELSE 0 END), 0) AS integer), " +
           "CAST(COALESCE(SUM(CASE WHEN w.storeType != 'MAIN' THEN i.quantityAvailable ELSE 0 END), 0) AS integer), " +
           "CAST(COALESCE(SUM(i.quantityAvailable), 0) AS integer)) " +
           "FROM InventoryItem i " +
           "JOIN i.product p " +
           "JOIN i.warehouse w " +
           "WHERE i.tenant.id = :tenantId " +
           "GROUP BY p.id, p.name, p.sku")
    java.util.List<com.spiceflow.backend.sales.dto.response.StockStatusResponse> getStockStatusReport(@Param("tenantId") Long tenantId);
}
