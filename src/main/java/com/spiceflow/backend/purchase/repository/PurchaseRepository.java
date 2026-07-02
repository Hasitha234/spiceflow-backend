package com.spiceflow.backend.purchase.repository;

import com.spiceflow.backend.purchase.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    
    Optional<Purchase> findByIdAndTenantId(Long id, Long tenantId);
    
    Page<Purchase> findByTenantId(Long tenantId, Pageable pageable);
    
    Page<Purchase> findByTenantIdAndInvoiceNoContainingIgnoreCase(Long tenantId, String invoiceNo, Pageable pageable);
}
