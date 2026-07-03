package com.spiceflow.backend.inventory.mapper;

import com.spiceflow.backend.inventory.dto.response.ProductResponse;
import com.spiceflow.backend.inventory.entity.Product;
import org.mapstruct.Mapper;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    ProductResponse toResponse(Product product);
}

