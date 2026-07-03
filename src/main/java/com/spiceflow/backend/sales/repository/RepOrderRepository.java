package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.RepOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepOrderRepository extends JpaRepository<RepOrder, Long> {
    Optional<RepOrder> findByIdAndTenantId(Long id, Long tenantId);
    Page<RepOrder> findByTenantId(Long tenantId, Pageable pageable);
    Page<RepOrder> findByTenantIdAndRepId(Long tenantId, Long repId, Pageable pageable);
}
