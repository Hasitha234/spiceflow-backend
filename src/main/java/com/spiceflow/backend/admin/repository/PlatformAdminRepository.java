package com.spiceflow.backend.admin.repository;

import com.spiceflow.backend.admin.entity.PlatformAdmin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Data access for the platform_admins table. */
@Repository
public interface PlatformAdminRepository extends JpaRepository<PlatformAdmin, Long> {

  Optional<PlatformAdmin> findByEmailAndDeletedAtIsNull(String email);
}
