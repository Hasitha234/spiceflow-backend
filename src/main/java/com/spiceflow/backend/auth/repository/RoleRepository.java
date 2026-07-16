package com.spiceflow.backend.auth.repository;

import com.spiceflow.backend.auth.entity.Role;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Data access for the roles table. */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  Page<Role> findByTenantIdAndDeletedAtIsNull(Long tenantId, Pageable pageable);

  Optional<Role> findByTenantIdAndNameAndDeletedAtIsNull(Long tenantId, String name);

  Optional<Role> findByIdAndTenantIdAndDeletedAtIsNull(Long id, Long tenantId);

  java.util.List<Role> findByTenantId(Long tenantId);
}
