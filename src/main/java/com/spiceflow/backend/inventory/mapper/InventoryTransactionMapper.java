package com.spiceflow.backend.inventory.mapper;

import com.spiceflow.backend.inventory.dto.response.InventoryTransactionResponse;
import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryTransactionMapper {

    @Mapping(target = "inventoryItemId", source = "inventoryItem.id")
    @Mapping(target = "productName", source = "inventoryItem.product.name")
    @Mapping(target = "warehouseName", source = "inventoryItem.warehouse.name")
    InventoryTransactionResponse toResponse(InventoryTransaction transaction);
}
