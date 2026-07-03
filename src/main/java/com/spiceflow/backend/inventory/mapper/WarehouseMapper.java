package com.spiceflow.backend.inventory.mapper;

import com.spiceflow.backend.inventory.dto.response.WarehouseResponse;
import com.spiceflow.backend.inventory.entity.Warehouse;
import org.mapstruct.Mapper;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface WarehouseMapper {

    WarehouseResponse toResponse(Warehouse warehouse);
}

