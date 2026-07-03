package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.LoadingSheetReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoadingSheetReturnRepository extends JpaRepository<LoadingSheetReturn, Long> {
    List<LoadingSheetReturn> findByLoadingSheetIdAndTenantId(Long loadingSheetId, Long tenantId);
}
