package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.Rep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepRepository extends JpaRepository<Rep, Long> {
    Optional<Rep> findByIdAndTenantId(Long id, Long tenantId);
    Page<Rep> findByTenantId(Long tenantId, Pageable pageable);
    Page<Rep> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name, Pageable pageable);
}
