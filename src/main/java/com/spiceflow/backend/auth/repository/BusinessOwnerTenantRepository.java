package com.spiceflow.backend.auth.repository;

import com.spiceflow.backend.auth.entity.BusinessOwnerTenant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessOwnerTenantRepository extends JpaRepository<BusinessOwnerTenant, Long> {
    List<BusinessOwnerTenant> findByUserId(Long userId);
    List<BusinessOwnerTenant> findByTenantId(Long tenantId);
    void deleteByUserIdAndTenantId(Long userId, Long tenantId);
}
