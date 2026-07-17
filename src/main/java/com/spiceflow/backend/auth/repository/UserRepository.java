package com.spiceflow.backend.auth.repository;

import com.spiceflow.backend.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.EntityGraph;

/** Data access for the users table. */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"assignedRole", "assignedRole.permissions"})
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByEmail(String email);
    
    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(id) > 0 FROM users WHERE email = :email", nativeQuery = true)
    boolean existsByEmailIncludingDeleted(@org.springframework.data.repository.query.Param("email") String email);
    boolean existsByAssignedRoleIdAndDeletedAtIsNull(Long roleId);
    org.springframework.data.domain.Page<User> findAllByDeletedAtIsNull(org.springframework.data.domain.Pageable pageable);
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.deletedAt IS NULL")
    java.util.List<User> findByTenantIdAndDeletedAtIsNull(@org.springframework.data.repository.query.Param("tenantId") Long tenantId);
}
