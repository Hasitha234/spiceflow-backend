package com.spiceflow.backend.sales.repository;

import com.spiceflow.backend.sales.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByIdAndTenantId(Long id, Long tenantId);
    Page<Delivery> findByTenantId(Long tenantId, Pageable pageable);
    Page<Delivery> findByTenantIdAndDeliveryDate(Long tenantId, java.time.LocalDate deliveryDate, Pageable pageable);
    
    @org.springframework.data.jpa.repository.Query("SELECT d FROM Delivery d WHERE d.tenant.id = :tenantId AND d.deliveryDate >= :startDate AND d.deliveryDate <= :endDate")
    java.util.List<Delivery> findDeliveriesInDateRange(
        @org.springframework.data.repository.query.Param("tenantId") Long tenantId,
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate
    );
}
