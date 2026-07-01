package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.Supplier;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByIdAndTenantId(Long id, Long tenantId);
}
