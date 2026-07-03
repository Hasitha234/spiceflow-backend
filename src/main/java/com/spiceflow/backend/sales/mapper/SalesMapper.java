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
    
    RepResponse toRepResponse(Rep rep);
    
    DriverResponse toDriverResponse(Driver driver);
}
