package com.spiceflow.backend.inventory.transfer.repository;

import com.spiceflow.backend.inventory.transfer.domain.WarehouseTransferState;
import com.spiceflow.backend.inventory.transfer.entity.WarehouseTransferEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseTransferRepository extends JpaRepository<WarehouseTransferEntity, Long> {

    Optional<WarehouseTransferEntity> findByTransferNumberAndTenantId(
        String transferNumber,
        Long tenantId
    );

    List<WarehouseTransferEntity> findByTenantIdAndStatus(
        Long tenantId,
        WarehouseTransferState status
    );
}
