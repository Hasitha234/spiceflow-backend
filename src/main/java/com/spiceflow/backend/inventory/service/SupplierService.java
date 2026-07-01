package com.spiceflow.backend.inventory.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.SupplierRequest;
import com.spiceflow.backend.inventory.dto.response.SupplierResponse;
import com.spiceflow.backend.inventory.entity.Supplier;
import com.spiceflow.backend.inventory.repository.SupplierRepository;
import com.spiceflow.backend.inventory.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final TenantRepository tenantRepository;
    private final SupplierMapper supplierMapper;

    @Transactional(rollbackFor = Exception.class)
    public SupplierResponse createSupplier(Long tenantId, SupplierRequest request) {
        log.debug("Creating supplier for tenantId: {}, name: {}", tenantId, request.getName());
        try {
            Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant with ID " + tenantId + " not found"));
            
            Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .taxId(request.getTaxId())
                .tenant(tenant)
                .build();
                
            Supplier savedSupplier = supplierRepository.save(supplier);
            log.info("Successfully created supplier with ID: {} for tenantId: {}", savedSupplier.getId(), tenantId);
            return supplierMapper.toResponse(savedSupplier);
        } catch (Exception e) {
            log.error("Failed to create supplier for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to create supplier: " + e.getMessage());
        }
    }
    
    public Page<SupplierResponse> getSuppliers(Long tenantId, String search, Pageable pageable) {
        log.debug("Fetching suppliers for tenantId: {}, search: {}", tenantId, search);
        try {
            Page<Supplier> supplierPage;
            if (search != null && !search.trim().isEmpty()) {
                supplierPage = supplierRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, search.trim(), pageable);
            } else {
                supplierPage = supplierRepository.findByTenantId(tenantId, pageable);
            }
            return supplierPage.map(supplierMapper::toResponse);
        } catch (Exception e) {
            log.error("Failed to fetch suppliers for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to fetch suppliers");
        }
    }
    
    public SupplierResponse getSupplier(Long tenantId, Long id) {
        log.debug("Fetching supplier with ID: {} for tenantId: {}", id, tenantId);
        return supplierMapper.toResponse(getSupplierEntity(tenantId, id));
    }
    
    @Transactional(rollbackFor = Exception.class)
    public SupplierResponse updateSupplier(Long tenantId, Long id, SupplierRequest request) {
        log.debug("Updating supplier with ID: {} for tenantId: {}", id, tenantId);
        try {
            Supplier supplier = getSupplierEntity(tenantId, id);
            
            supplier.setName(request.getName());
            supplier.setContactEmail(request.getContactEmail());
            supplier.setContactPhone(request.getContactPhone());
            supplier.setAddress(request.getAddress());
            supplier.setTaxId(request.getTaxId());
            
            Supplier updatedSupplier = supplierRepository.save(supplier);
            log.info("Successfully updated supplier with ID: {} for tenantId: {}", id, tenantId);
            return supplierMapper.toResponse(updatedSupplier);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update supplier with ID: {} for tenantId: {}", id, tenantId, e);
            throw new BusinessRuleViolationException("Failed to update supplier: " + e.getMessage());
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteSupplier(Long tenantId, Long id) {
        log.debug("Deleting supplier with ID: {} for tenantId: {}", id, tenantId);
        try {
            Supplier supplier = getSupplierEntity(tenantId, id);
            supplierRepository.delete(supplier);
            log.info("Successfully deleted supplier with ID: {} for tenantId: {}", id, tenantId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete supplier with ID: {} for tenantId: {}", id, tenantId, e);
            throw new BusinessRuleViolationException("Failed to delete supplier due to existing dependencies");
        }
    }
    
    public Supplier getSupplierEntity(Long tenantId, Long id) {
        return supplierRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> {
                log.error("Supplier not found with ID: {} for tenantId: {}", id, tenantId);
                return new ResourceNotFoundException("Supplier not found with id: " + id);
            });
    }
}
