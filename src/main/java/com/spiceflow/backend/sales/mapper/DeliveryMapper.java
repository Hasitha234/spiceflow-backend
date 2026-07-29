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
    @Mapping(target = "targetShopNames", expression = "java(buildTargetShopNames(delivery))")
    @Mapping(target = "targetOutletIds", expression = "java(buildTargetOutletIds(delivery))")
    DeliveryResponse toResponse(Delivery delivery);
    
    default String buildTargetShopNames(Delivery delivery) {
        if (delivery.getLoadingSheet() != null && delivery.getLoadingSheet().getRepOrder() != null && delivery.getLoadingSheet().getRepOrder().getShops() != null) {
            return delivery.getLoadingSheet().getRepOrder().getShops().stream()
                .map(s -> s.getShop() != null ? s.getShop().getName() : null)
                .filter(name -> name != null && !name.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
        }
        return null;
    }
    
    default String buildTargetOutletIds(Delivery delivery) {
        if (delivery.getLoadingSheet() != null && delivery.getLoadingSheet().getRepOrder() != null && delivery.getLoadingSheet().getRepOrder().getShops() != null) {
            return delivery.getLoadingSheet().getRepOrder().getShops().stream()
                .map(s -> s.getShop() != null ? s.getShop().getOutletId() : null)
                .filter(id -> id != null && !id.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
        }
        return null;
    }
    
    @Mapping(source = "shop.id", target = "shopId")
    @Mapping(source = "shop.name", target = "shopName")
    @Mapping(target = "qrScannedAt", ignore = true)
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
