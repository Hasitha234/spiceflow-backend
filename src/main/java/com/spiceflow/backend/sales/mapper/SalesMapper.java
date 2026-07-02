package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.sales.dto.response.DriverResponse;
import com.spiceflow.backend.sales.dto.response.RepResponse;
import com.spiceflow.backend.sales.dto.response.ShopResponse;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.entity.Shop;
import org.springframework.stereotype.Component;

@Component
public class SalesMapper {
    
    public ShopResponse toShopResponse(Shop shop) {
        if (shop == null) return null;
        return ShopResponse.builder()
            .id(shop.getId())
            .name(shop.getName())
            .ownerName(shop.getOwnerName())
            .phone(shop.getPhone())
            .address(shop.getAddress())
            .area(shop.getArea())
            .route(shop.getRoute())
            .assignedRepId(shop.getAssignedRep() != null ? shop.getAssignedRep().getId() : null)
            .assignedRepName(shop.getAssignedRep() != null ? shop.getAssignedRep().getName() : null)
            .outstandingLoan(shop.getOutstandingLoan())
            .createdAt(shop.getCreatedAt())
            .updatedAt(shop.getUpdatedAt())
            .build();
    }
    
    public RepResponse toRepResponse(Rep rep) {
        if (rep == null) return null;
        return RepResponse.builder()
            .id(rep.getId())
            .name(rep.getName())
            .phone(rep.getPhone())
            .area(rep.getArea())
            .isActive(rep.getIsActive())
            .createdAt(rep.getCreatedAt())
            .updatedAt(rep.getUpdatedAt())
            .build();
    }
    
    public DriverResponse toDriverResponse(Driver driver) {
        if (driver == null) return null;
        return DriverResponse.builder()
            .id(driver.getId())
            .name(driver.getName())
            .phone(driver.getPhone())
            .vehicleNo(driver.getVehicleNo())
            .isActive(driver.getIsActive())
            .createdAt(driver.getCreatedAt())
            .updatedAt(driver.getUpdatedAt())
            .build();
    }
}
