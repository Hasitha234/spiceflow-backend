package com.spiceflow.backend.inventory.mapper;

import com.spiceflow.backend.inventory.dto.response.ProductCategoryResponse;
import com.spiceflow.backend.inventory.entity.ProductCategory;
import org.mapstruct.Mapper;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface ProductCategoryMapper {

    @Mapping(target = "parentCategoryId", source = "parentCategory.id")
    ProductCategoryResponse toResponse(ProductCategory category);
}

