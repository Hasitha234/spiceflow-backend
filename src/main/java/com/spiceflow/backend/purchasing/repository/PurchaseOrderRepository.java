package com.spiceflow.backend.purchasing.repository;

import com.spiceflow.backend.purchasing.domain.PurchaseOrderState;
import com.spiceflow.backend.purchasing.entity.PurchaseOrderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {

    Optional<PurchaseOrderEntity> findByCorrelationIdAndTenantId(
        String correlationId,
        Long tenantId
    );

    List<PurchaseOrderEntity> findByTenantIdAndStatus(
        Long tenantId,
        PurchaseOrderState status
    );
}
