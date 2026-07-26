package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
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
    private final WarehouseRepository warehouseRepository;
    private final TenantRepository tenantRepository;
    private final SalesMapper salesMapper;
    
    // --- REP ---
    @Transactional(rollbackFor = Exception.class)
    public RepResponse createRep(Long tenantId, RepRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        Rep rep = Rep.builder()
            .tenant(tenant)
            .employeeId(request.employeeId())
            .name(request.name())
            .email(request.email())
            .phone(request.phone())
            .area(request.area())
            .employmentDate(request.employmentDate())
            .terminationDate(request.terminationDate())
            .isActive(request.isActive() != null ? request.isActive() : true)
            .build();
            
        return salesMapper.toRepResponseWithCount(repRepository.save(rep), 0L);
    }
    
    public Page<RepResponse> getReps(Long tenantId, String name, Pageable pageable) {
        Page<Rep> reps;
        if (name != null && !name.isBlank()) {
            reps = repRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, name, pageable);
        } else {
            reps = repRepository.findByTenantId(tenantId, pageable);
        }
        java.util.Map<Long, Long> countsMap = shopRepository.countShopsByAssignedRepId(tenantId).stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).longValue()
            ));
        return reps.map(rep -> salesMapper.toRepResponseWithCount(rep, countsMap.getOrDefault(rep.getId(), 0L)));
    }

    public RepResponse getRep(Long id, Long tenantId) {
        Rep rep = getRepEntity(id, tenantId);
        return salesMapper.toRepResponse(rep);
    }

    @Transactional(rollbackFor = Exception.class)
    public RepResponse updateRep(Long id, Long tenantId, RepRequest request) {
        Rep rep = getRepEntity(id, tenantId);
        rep.setEmployeeId(request.employeeId());
        rep.setName(request.name());
        rep.setEmail(request.email());
        rep.setPhone(request.phone());
        rep.setArea(request.area());
        rep.setEmploymentDate(request.employmentDate());
        rep.setTerminationDate(request.terminationDate());
        if (request.isActive() != null) {
            rep.setIsActive(request.isActive());
        }
        return salesMapper.toRepResponse(repRepository.save(rep));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRep(Long id, Long tenantId) {
        Rep rep = getRepEntity(id, tenantId);
        repRepository.delete(rep);
    }
    
    // --- DRIVER ---
    @Transactional(rollbackFor = Exception.class)
    public DriverResponse createDriver(Long tenantId, DriverRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        Warehouse defaultWarehouse = null;
        if (request.defaultWarehouseId() != null) {
            defaultWarehouse = warehouseRepository.findByIdAndTenantId(request.defaultWarehouseId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        }
            
        Driver driver = Driver.builder()
            .tenant(tenant)
            .name(request.name())
            .employeeId(request.employeeId())
            .email(request.email())
            .phone(request.phone())
            .employmentDate(request.employmentDate())
            .terminationDate(request.terminationDate())
            .licenseNumber(request.licenseNumber())
            .licenseClass(request.licenseClass())
            .licenseExpiry(request.licenseExpiry())
            .defaultWarehouse(defaultWarehouse)
            .assignedVehicle(request.assignedVehicle())
            .status(request.status() != null ? request.status() : com.spiceflow.backend.common.enums.DriverStatus.AVAILABLE)
            .isActive(request.isActive() != null ? request.isActive() : true)
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

    public DriverResponse getDriver(Long id, Long tenantId) {
        return salesMapper.toDriverResponse(getDriverEntity(id, tenantId));
    }

    @Transactional(rollbackFor = Exception.class)
    public DriverResponse updateDriver(Long id, Long tenantId, DriverRequest request) {
        Driver driver = getDriverEntity(id, tenantId);

        Warehouse defaultWarehouse = null;
        if (request.defaultWarehouseId() != null) {
            defaultWarehouse = warehouseRepository.findByIdAndTenantId(request.defaultWarehouseId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        }

        driver.setName(request.name());
        driver.setEmployeeId(request.employeeId());
        driver.setEmail(request.email());
        driver.setPhone(request.phone());
        driver.setEmploymentDate(request.employmentDate());
        driver.setTerminationDate(request.terminationDate());
        driver.setLicenseNumber(request.licenseNumber());
        driver.setLicenseClass(request.licenseClass());
        driver.setLicenseExpiry(request.licenseExpiry());
        driver.setDefaultWarehouse(defaultWarehouse);
        driver.setAssignedVehicle(request.assignedVehicle());
        if (request.status() != null) {
            driver.setStatus(request.status());
        }
        if (request.isActive() != null) {
            driver.setIsActive(request.isActive());
        }

        return salesMapper.toDriverResponse(driverRepository.save(driver));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDriver(Long id, Long tenantId) {
        Driver driver = getDriverEntity(id, tenantId);
        driverRepository.delete(driver);
    }
    
    // --- SHOP ---
    @Transactional(rollbackFor = Exception.class)
    public ShopResponse createShop(Long tenantId, ShopRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        Rep assignedRep = null;
        if (request.assignedRepId() != null) {
            assignedRep = repRepository.findByIdAndTenantId(request.assignedRepId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));
        }
            
        Shop shop = Shop.builder()
            .tenant(tenant)
            .name(request.name())
            .outletId(request.outletId())
            .phone(request.phone())
            .address(request.address())
            .area(request.area())
            .route(request.route())
            .assignedRep(assignedRep)
            .outstandingLoan(request.outstandingLoan() != null ? request.outstandingLoan() : java.math.BigDecimal.ZERO)
            .latitude(request.latitude())
            .longitude(request.longitude())
            .isActive(request.isActive() != null ? request.isActive() : true)
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

    public ShopResponse getShop(Long id, Long tenantId) {
        return salesMapper.toShopResponse(getShopEntity(id, tenantId));
    }

    @Transactional(rollbackFor = Exception.class)
    public ShopResponse updateShop(Long id, Long tenantId, ShopRequest request) {
        Shop shop = getShopEntity(id, tenantId);

        Rep assignedRep = null;
        if (request.assignedRepId() != null) {
            assignedRep = repRepository.findByIdAndTenantId(request.assignedRepId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));
        }

        shop.setName(request.name());
        shop.setOutletId(request.outletId());
        shop.setPhone(request.phone());
        shop.setAddress(request.address());
        shop.setArea(request.area());
        shop.setRoute(request.route());
        shop.setAssignedRep(assignedRep);
        if (request.outstandingLoan() != null) {
            shop.setOutstandingLoan(request.outstandingLoan());
        }
        shop.setLatitude(request.latitude());
        shop.setLongitude(request.longitude());
        if (request.isActive() != null) {
            shop.setIsActive(request.isActive());
        }

        return salesMapper.toShopResponse(shopRepository.save(shop));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteShop(Long id, Long tenantId) {
        Shop shop = getShopEntity(id, tenantId);
        shopRepository.delete(shop);
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
