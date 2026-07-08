package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.sales.dto.response.RepOrderItemResponse;
import com.spiceflow.backend.sales.dto.response.RepOrderResponse;
import com.spiceflow.backend.sales.dto.response.RepOrderShopResponse;
import com.spiceflow.backend.sales.dto.response.ShopReturnResponse;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.entity.RepOrderItem;
import com.spiceflow.backend.sales.entity.RepOrderShop;
import com.spiceflow.backend.sales.entity.ShopReturn;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface RepOrderMapper {
    
    @Mapping(source = "rep.id", target = "repId")
    @Mapping(source = "rep.name", target = "repName")
    RepOrderResponse toResponse(RepOrder repOrder);
    
    @Mapping(source = "shop.id", target = "shopId")
    @Mapping(source = "shop.name", target = "shopName")
    @Mapping(source = "returnWarehouse.id", target = "returnWarehouseId")
    @Mapping(source = "returnWarehouse.name", target = "returnWarehouseName")
    RepOrderShopResponse toShopResponse(RepOrderShop shop);
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    RepOrderItemResponse toItemResponse(RepOrderItem item);
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    ShopReturnResponse toReturnResponse(ShopReturn shopReturn);
}
