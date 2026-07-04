package com.spiceflow.backend.inventory.transfer.repository;

import com.spiceflow.backend.inventory.transfer.entity.WarehouseTransferLineEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseTransferLineRepository extends JpaRepository<WarehouseTransferLineEntity, Long> {

    List<WarehouseTransferLineEntity> findByWarehouseTransferId(Long warehouseTransferId);
}
