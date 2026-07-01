package com.spiceflow.backend.inventory.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.inventory.dto.request.SupplierRequest;
import com.spiceflow.backend.inventory.dto.response.SupplierResponse;
import com.spiceflow.backend.inventory.entity.Supplier;
import com.spiceflow.backend.inventory.repository.SupplierRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final TenantRepository tenantRepository;

    public List<SupplierResponse> getAllSuppliers(Long tenantId) {
        // We'll add pagination later. For V1 MVP, a simple list is fine.
        return supplierRepository.findAll().stream()
                .filter(s -> s.getTenant().getId().equals(tenantId))
                .map(SupplierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SupplierResponse getSupplier(Long tenantId, Long supplierId) {
        Supplier supplier = getSupplierEntity(tenantId, supplierId);
        return SupplierResponse.fromEntity(supplier);
    }

    @Transactional
    public SupplierResponse createSupplier(Long tenantId, SupplierRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .taxId(request.getTaxId())
                .tenant(tenant)
                .build();

        return SupplierResponse.fromEntity(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse updateSupplier(Long tenantId, Long supplierId, SupplierRequest request) {
        Supplier supplier = getSupplierEntity(tenantId, supplierId);

        supplier.setName(request.getName());
        supplier.setContactEmail(request.getContactEmail());
        supplier.setContactPhone(request.getContactPhone());
        supplier.setAddress(request.getAddress());
        supplier.setTaxId(request.getTaxId());

        return SupplierResponse.fromEntity(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(Long tenantId, Long supplierId) {
        Supplier supplier = getSupplierEntity(tenantId, supplierId);
        supplierRepository.delete(supplier);
    }

    private Supplier getSupplierEntity(Long tenantId, Long supplierId) {
        return supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    }
}
