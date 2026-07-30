package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.CancelSummaryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelSummaryItemRepository extends JpaRepository<CancelSummaryItem, Long> {
}
