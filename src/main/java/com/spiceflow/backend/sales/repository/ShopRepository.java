package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByIdAndTenantId(Long id, Long tenantId);
    Page<Shop> findByTenantId(Long tenantId, Pageable pageable);
    Page<Shop> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name, Pageable pageable);

    @Query("SELECT s.assignedRep.id, COUNT(s) FROM Shop s WHERE s.tenant.id = :tenantId AND s.assignedRep IS NOT NULL GROUP BY s.assignedRep.id")
    List<Object[]> countShopsByAssignedRepId(@Param("tenantId") Long tenantId);

    Optional<Shop> findByQrCodeTokenAndTenantId(String qrCodeToken, Long tenantId);

    boolean existsByTenantIdAndOutletIdIgnoreCase(Long tenantId, String outletId);
    boolean existsByTenantIdAndOutletIdIgnoreCaseAndIdNot(Long tenantId, String outletId, Long id);
}
