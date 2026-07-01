package com.spiceflow.backend.inventory.mapper;

import com.spiceflow.backend.inventory.dto.response.ProductCategoryResponse;
import com.spiceflow.backend.inventory.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {

    @Mapping(target = "parentCategoryId", source = "parentCategory.id")
    ProductCategoryResponse toResponse(ProductCategory category);
}
