package com.spiceflow.backend.auth.repository;

import com.spiceflow.backend.auth.entity.Permission;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Data access for the permissions table. */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

  Optional<Permission> findByCode(String code);

  List<Permission> findByModule(String module);

  Set<Permission> findByCodeIn(Set<String> codes);
}
