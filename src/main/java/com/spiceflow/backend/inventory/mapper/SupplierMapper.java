package com.spiceflow.backend.inventory.mapper;

import com.spiceflow.backend.inventory.dto.response.SupplierResponse;
import com.spiceflow.backend.inventory.entity.Supplier;
import org.mapstruct.Mapper;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface SupplierMapper {

    SupplierResponse toResponse(Supplier supplier);
}

