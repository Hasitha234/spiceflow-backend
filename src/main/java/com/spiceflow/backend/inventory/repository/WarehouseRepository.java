package com.spiceflow.backend.inventory.repository;

import com.spiceflow.backend.inventory.entity.Warehouse;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByIdAndTenantId(Long id, Long tenantId);
}
