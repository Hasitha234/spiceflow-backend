package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.sales.dto.request.DriverRequest;
import com.spiceflow.backend.sales.dto.request.RepRequest;
import com.spiceflow.backend.sales.dto.request.ShopRequest;
import com.spiceflow.backend.sales.dto.response.DriverResponse;
import com.spiceflow.backend.sales.dto.response.RepResponse;
import com.spiceflow.backend.sales.dto.response.ShopResponse;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.mapper.SalesMapper;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
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
public class SalesMasterDataService {

    private final ShopRepository shopRepository;
    private final RepRepository repRepository;
    private final DriverRepository driverRepository;
    private final TenantRepository tenantRepository;
    private final SalesMapper salesMapper;
    
    // --- REP ---
    @Transactional(rollbackFor = Exception.class)
    public RepResponse createRep(Long tenantId, RepRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        Rep rep = Rep.builder()
            .tenant(tenant)
            .name(request.getName())
            .phone(request.getPhone())
            .area(request.getArea())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .build();
            
        return salesMapper.toRepResponse(repRepository.save(rep));
    }
    
    public Page<RepResponse> getReps(Long tenantId, String name, Pageable pageable) {
        Page<Rep> reps;
        if (name != null && !name.isBlank()) {
            reps = repRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, name, pageable);
        } else {
            reps = repRepository.findByTenantId(tenantId, pageable);
        }
        return reps.map(salesMapper::toRepResponse);
    }
    
    // --- DRIVER ---
    @Transactional(rollbackFor = Exception.class)
    public DriverResponse createDriver(Long tenantId, DriverRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        Driver driver = Driver.builder()
            .tenant(tenant)
            .name(request.getName())
            .phone(request.getPhone())
            .vehicleNo(request.getVehicleNo())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .build();
            
        return salesMapper.toDriverResponse(driverRepository.save(driver));
    }
    
    public Page<DriverResponse> getDrivers(Long tenantId, String name, Pageable pageable) {
        Page<Driver> drivers;
        if (name != null && !name.isBlank()) {
            drivers = driverRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, name, pageable);
        } else {
            drivers = driverRepository.findByTenantId(tenantId, pageable);
        }
        return drivers.map(salesMapper::toDriverResponse);
    }
    
    // --- SHOP ---
    @Transactional(rollbackFor = Exception.class)
    public ShopResponse createShop(Long tenantId, ShopRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        Rep assignedRep = null;
        if (request.getAssignedRepId() != null) {
            assignedRep = repRepository.findByIdAndTenantId(request.getAssignedRepId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));
        }
            
        Shop shop = Shop.builder()
            .tenant(tenant)
            .name(request.getName())
            .ownerName(request.getOwnerName())
            .phone(request.getPhone())
            .address(request.getAddress())
            .area(request.getArea())
            .route(request.getRoute())
            .assignedRep(assignedRep)
            .outstandingLoan(request.getOutstandingLoan() != null ? request.getOutstandingLoan() : java.math.BigDecimal.ZERO)
            .build();
            
        return salesMapper.toShopResponse(shopRepository.save(shop));
    }
    
    public Page<ShopResponse> getShops(Long tenantId, String name, Pageable pageable) {
        Page<Shop> shops;
        if (name != null && !name.isBlank()) {
            shops = shopRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, name, pageable);
        } else {
            shops = shopRepository.findByTenantId(tenantId, pageable);
        }
        return shops.map(salesMapper::toShopResponse);
    }
    
    public Shop getShopEntity(Long id, Long tenantId) {
        return shopRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
    }
    
    public Rep getRepEntity(Long id, Long tenantId) {
        return repRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));
    }
    
    public Driver getDriverEntity(Long id, Long tenantId) {
        return driverRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
    }
}
