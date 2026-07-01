package com.spiceflow.backend.inventory.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.WarehouseRequest;
import com.spiceflow.backend.inventory.dto.response.WarehouseResponse;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.inventory.mapper.WarehouseMapper;
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
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final TenantRepository tenantRepository;
    private final WarehouseMapper warehouseMapper;

    @Transactional(rollbackFor = Exception.class)
    public WarehouseResponse createWarehouse(Long tenantId, WarehouseRequest request) {
        log.debug("Creating new warehouse for tenantId: {}, name: {}", tenantId, request.getName());
        try {
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Tenant with ID " + tenantId + " not found"));

            Warehouse warehouse = Warehouse.builder()
                    .name(request.getName())
                    .location(request.getLocation())
                    .capacity(request.getCapacity())
                    .tenant(tenant)
                    .build();

            Warehouse savedWarehouse = warehouseRepository.save(warehouse);
            log.info("Successfully created warehouse with ID: {} for tenantId: {}", savedWarehouse.getId(), tenantId);
            return warehouseMapper.toResponse(savedWarehouse);
        } catch (Exception e) {
            log.error("Failed to create warehouse for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to create warehouse: " + e.getMessage());
        }
    }
    
    public Page<WarehouseResponse> getAllWarehouses(Long tenantId, String search, Pageable pageable) {
        log.debug("Fetching warehouses for tenantId: {}, search: {}", tenantId, search);
        try {
            Page<Warehouse> warehousePage;
            if (search != null && !search.trim().isEmpty()) {
                warehousePage = warehouseRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, search.trim(), pageable);
            } else {
                warehousePage = warehouseRepository.findByTenantId(tenantId, pageable);
            }
            return warehousePage.map(warehouseMapper::toResponse);
        } catch (Exception e) {
            log.error("Failed to fetch warehouses for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to fetch warehouses");
        }
    }
    
    public WarehouseResponse getWarehouse(Long tenantId, Long warehouseId) {
        log.debug("Fetching warehouse with ID: {} for tenantId: {}", warehouseId, tenantId);
        return warehouseMapper.toResponse(getWarehouseEntity(tenantId, warehouseId));
    }

    @Transactional(rollbackFor = Exception.class)
    public WarehouseResponse updateWarehouse(Long tenantId, Long warehouseId, WarehouseRequest request) {
        log.debug("Updating warehouse with ID: {} for tenantId: {}", warehouseId, tenantId);
        try {
            Warehouse warehouse = getWarehouseEntity(tenantId, warehouseId);

            warehouse.setName(request.getName());
            warehouse.setLocation(request.getLocation());
            warehouse.setCapacity(request.getCapacity());

            Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
            log.info("Successfully updated warehouse with ID: {} for tenantId: {}", updatedWarehouse.getId(), tenantId);
            return warehouseMapper.toResponse(updatedWarehouse);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update warehouse with ID: {} for tenantId: {}", warehouseId, tenantId, e);
            throw new BusinessRuleViolationException("Failed to update warehouse: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteWarehouse(Long tenantId, Long warehouseId) {
        log.debug("Deleting warehouse with ID: {} for tenantId: {}", warehouseId, tenantId);
        try {
            Warehouse warehouse = getWarehouseEntity(tenantId, warehouseId);
            warehouseRepository.delete(warehouse);
            log.info("Successfully deleted warehouse with ID: {} for tenantId: {}", warehouseId, tenantId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete warehouse with ID: {} for tenantId: {}", warehouseId, tenantId, e);
            throw new BusinessRuleViolationException("Failed to delete warehouse due to existing dependencies");
        }
    }

    public Warehouse getWarehouseEntity(Long tenantId, Long warehouseId) {
        return warehouseRepository.findByIdAndTenantId(warehouseId, tenantId)
                .orElseThrow(() -> {
                    log.error("Warehouse not found with ID: {} for tenantId: {}", warehouseId, tenantId);
                    return new ResourceNotFoundException("Warehouse not found with id: " + warehouseId);
                });
    }
}
