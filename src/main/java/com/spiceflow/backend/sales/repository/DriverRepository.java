package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Driver repository.
 *
 * ROOT CAUSE FIX (500 on GET /drivers):
 * The Driver entity has a LAZY ManyToOne to Warehouse (defaultWarehouse).
 * Warehouse carries @Filter(name="tenantFilter"). When MapStruct's generated
 * toDriverResponse() accesses driver.getDefaultWarehouse(), Hibernate fires a
 * secondary SELECT on the warehouses table and tries to apply the tenant filter.
 * The filter parameter is not reliably set at that point in the execution stack
 * (outside the AOP interceptor window), causing an IllegalStateException -> 500.
 *
 * Fix: Use explicit JPQL with LEFT JOIN FETCH so the warehouse is loaded
 * in the same query as the driver, eliminating the secondary lazy-load
 * entirely and making the tenant filter irrelevant for this association.
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Query("SELECT d FROM Driver d LEFT JOIN FETCH d.defaultWarehouse WHERE d.id = :id AND d.tenant.id = :tenantId")
    Optional<Driver> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query(
        value = "SELECT d FROM Driver d LEFT JOIN FETCH d.defaultWarehouse WHERE d.tenant.id = :tenantId",
        countQuery = "SELECT COUNT(d) FROM Driver d WHERE d.tenant.id = :tenantId"
    )
    Page<Driver> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query(
        value = "SELECT d FROM Driver d LEFT JOIN FETCH d.defaultWarehouse WHERE d.tenant.id = :tenantId AND LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))",
        countQuery = "SELECT COUNT(d) FROM Driver d WHERE d.tenant.id = :tenantId AND LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))"
    )
    Page<Driver> findByTenantIdAndNameContainingIgnoreCase(@Param("tenantId") Long tenantId, @Param("name") String name, Pageable pageable);
}
