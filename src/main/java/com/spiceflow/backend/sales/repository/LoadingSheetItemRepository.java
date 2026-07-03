package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.LoadingSheetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoadingSheetItemRepository extends JpaRepository<LoadingSheetItem, Long> {
    List<LoadingSheetItem> findByLoadingSheetIdAndTenantId(Long loadingSheetId, Long tenantId);
}
