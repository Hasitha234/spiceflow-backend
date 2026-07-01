package com.spiceflow.backend.inventory.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.inventory.dto.request.WarehouseRequest;
import com.spiceflow.backend.inventory.dto.response.WarehouseResponse;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final TenantRepository tenantRepository;

    public List<WarehouseResponse> getAllWarehouses(Long tenantId) {
        return warehouseRepository.findAll().stream()
                .filter(w -> w.getTenant().getId().equals(tenantId))
                .map(WarehouseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public WarehouseResponse getWarehouse(Long tenantId, Long warehouseId) {
        Warehouse warehouse = getWarehouseEntity(tenantId, warehouseId);
        return WarehouseResponse.fromEntity(warehouse);
    }

    @Transactional
    public WarehouseResponse createWarehouse(Long tenantId, WarehouseRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .tenant(tenant)
                .build();

        return WarehouseResponse.fromEntity(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseResponse updateWarehouse(Long tenantId, Long warehouseId, WarehouseRequest request) {
        Warehouse warehouse = getWarehouseEntity(tenantId, warehouseId);

        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setCapacity(request.getCapacity());

        return WarehouseResponse.fromEntity(warehouseRepository.save(warehouse));
    }

    @Transactional
    public void deleteWarehouse(Long tenantId, Long warehouseId) {
        Warehouse warehouse = getWarehouseEntity(tenantId, warehouseId);
        warehouseRepository.delete(warehouse);
    }

    private Warehouse getWarehouseEntity(Long tenantId, Long warehouseId) {
        return warehouseRepository.findByIdAndTenantId(warehouseId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
    }
}
