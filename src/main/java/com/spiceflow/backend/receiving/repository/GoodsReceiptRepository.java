package com.spiceflow.backend.receiving.repository;

import com.spiceflow.backend.receiving.domain.GoodsReceiptState;
import com.spiceflow.backend.receiving.entity.GoodsReceiptEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceiptEntity, Long> {

    Optional<GoodsReceiptEntity> findByReceiptNumberAndTenantId(
        String receiptNumber,
        Long tenantId
    );

    List<GoodsReceiptEntity> findByTenantIdAndStatus(
        Long tenantId,
        GoodsReceiptState status
    );
}
