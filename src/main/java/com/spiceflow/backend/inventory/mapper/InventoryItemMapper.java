package com.spiceflow.backend.inventory.mapper;

import com.spiceflow.backend.inventory.dto.response.InventoryItemResponse;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import org.mapstruct.Mapper;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface InventoryItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productCategoryName", source = "product.category.name")
    @Mapping(target = "productBasePrice", source = "product.basePrice")
    @Mapping(target = "unitOfMeasure", source = "product.unitOfMeasure")
    @Mapping(target = "itemsPerSoldUnit", source = "product.itemsPerSoldUnit")
    @Mapping(target = "soldUnitsPerBox", source = "product.soldUnitsPerBox")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    InventoryItemResponse toResponse(InventoryItem item);
}

