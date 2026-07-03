package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByIdAndTenantId(Long id, Long tenantId);
    Page<Driver> findByTenantId(Long tenantId, Pageable pageable);
    Page<Driver> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name, Pageable pageable);
}
