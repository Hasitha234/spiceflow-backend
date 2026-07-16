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
    boolean existsByAssignedRoleIdAndDeletedAtIsNull(Long roleId);
    org.springframework.data.domain.Page<User> findAllByDeletedAtIsNull(org.springframework.data.domain.Pageable pageable);
}
