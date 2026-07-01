package com.spiceflow.backend.admin.repository;

import com.spiceflow.backend.admin.entity.BusinessType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {
    Optional<BusinessType> findByName(String name);
    boolean existsByName(String name);
}
