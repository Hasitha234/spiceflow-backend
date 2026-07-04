package com.spiceflow.backend.purchasing.repository;

import com.spiceflow.backend.purchasing.entity.PurchaseOrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLineEntity, Long> {
}
