package com.spiceflow.backend.auth.repository;

import com.spiceflow.backend.auth.entity.Tenant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Data access for the tenants table. */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByEmail(String email);
    boolean existsByEmail(String email);
    
    /** Find a tenant by email, excluding soft-deleted ones. */
    Optional<Tenant> findByEmailAndDeletedAtIsNull(String email);

    /** Returns all active tenants for platform admins, with pagination. */
    Page<Tenant> findAllByDeletedAtIsNull(Pageable pageable);
    
    long countByBusinessTypeId(Long businessTypeId);
}
