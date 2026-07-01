package com.spiceflow.backend.inventory.mapper;

import com.spiceflow.backend.inventory.dto.response.SupplierResponse;
import com.spiceflow.backend.inventory.entity.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponse toResponse(Supplier supplier);
}
