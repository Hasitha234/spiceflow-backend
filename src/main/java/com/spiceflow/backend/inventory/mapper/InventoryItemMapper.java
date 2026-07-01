package com.spiceflow.backend.inventory.mapper;

import com.spiceflow.backend.inventory.dto.response.InventoryItemResponse;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    InventoryItemResponse toResponse(InventoryItem item);
}
