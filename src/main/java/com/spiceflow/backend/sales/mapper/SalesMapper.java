package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.sales.dto.response.DriverResponse;
import com.spiceflow.backend.sales.dto.response.RepResponse;
import com.spiceflow.backend.sales.dto.response.ShopResponse;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface SalesMapper {
    
    @Mapping(source = "assignedRep.id", target = "assignedRepId")
    @Mapping(source = "assignedRep.name", target = "assignedRepName")
    ShopResponse toShopResponse(Shop shop);
    
    @Mapping(target = "assignedShopsCount", ignore = true)
    RepResponse toRepResponse(Rep rep);

    @Mapping(target = "id", source = "rep.id")
    @Mapping(target = "employeeId", source = "rep.employeeId")
    @Mapping(target = "name", source = "rep.name")
    @Mapping(target = "email", source = "rep.email")
    @Mapping(target = "phone", source = "rep.phone")
    @Mapping(target = "area", source = "rep.area")
    @Mapping(target = "employmentDate", source = "rep.employmentDate")
    @Mapping(target = "terminationDate", source = "rep.terminationDate")
    @Mapping(target = "isActive", source = "rep.isActive")
    @Mapping(target = "createdAt", source = "rep.createdAt")
    @Mapping(target = "updatedAt", source = "rep.updatedAt")
    @Mapping(target = "assignedShopsCount", source = "assignedShopsCount")
    RepResponse toRepResponseWithCount(Rep rep, Long assignedShopsCount);
    
    DriverResponse toDriverResponse(Driver driver);
}
