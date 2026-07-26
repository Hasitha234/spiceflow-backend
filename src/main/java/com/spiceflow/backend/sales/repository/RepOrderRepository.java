package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.RepOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RepOrderRepository extends JpaRepository<RepOrder, Long> {
    Optional<RepOrder> findByIdAndTenantId(Long id, Long tenantId);
    Page<RepOrder> findByTenantId(Long tenantId, Pageable pageable);
    Page<RepOrder> findByTenantIdAndRepId(Long tenantId, Long repId, Pageable pageable);
    Page<RepOrder> findByTenantIdAndOrderDate(Long tenantId, LocalDate orderDate, Pageable pageable);
    Page<RepOrder> findByTenantIdAndRepIdAndOrderDate(Long tenantId, Long repId, LocalDate orderDate, Pageable pageable);
    
    java.util.List<RepOrder> findByTenantIdAndOrderDateBetween(Long tenantId, LocalDate startDate, LocalDate endDate);

    Optional<RepOrder> findTopByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
