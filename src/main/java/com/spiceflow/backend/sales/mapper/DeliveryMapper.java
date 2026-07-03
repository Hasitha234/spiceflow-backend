package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.sales.dto.response.DeliveryPaymentResponse;
import com.spiceflow.backend.sales.dto.response.DeliveryResponse;
import com.spiceflow.backend.sales.dto.response.DeliveryShopItemResponse;
import com.spiceflow.backend.sales.dto.response.DeliveryShopResponse;
import com.spiceflow.backend.sales.dto.response.DeliveryShopReturnResponse;
import com.spiceflow.backend.sales.entity.Delivery;
import com.spiceflow.backend.sales.entity.DeliveryPayment;
import com.spiceflow.backend.sales.entity.DeliveryShop;
import com.spiceflow.backend.sales.entity.DeliveryShopItem;
import com.spiceflow.backend.sales.entity.DeliveryShopReturn;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface DeliveryMapper {
    
    @Mapping(source = "loadingSheet.id", target = "loadingSheetId")
    DeliveryResponse toResponse(Delivery delivery);
    
    @Mapping(source = "shop.id", target = "shopId")
    @Mapping(source = "shop.name", target = "shopName")
    DeliveryShopResponse toShopResponse(DeliveryShop shop);
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    DeliveryShopItemResponse toItemResponse(DeliveryShopItem item);
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    DeliveryShopReturnResponse toReturnResponse(DeliveryShopReturn ret);
    
    DeliveryPaymentResponse toPaymentResponse(DeliveryPayment payment);
}
