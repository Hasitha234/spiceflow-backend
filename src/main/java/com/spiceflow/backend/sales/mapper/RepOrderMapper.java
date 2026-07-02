package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.sales.dto.response.RepOrderItemResponse;
import com.spiceflow.backend.sales.dto.response.RepOrderResponse;
import com.spiceflow.backend.sales.dto.response.RepOrderShopResponse;
import com.spiceflow.backend.sales.dto.response.ShopReturnResponse;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.entity.RepOrderItem;
import com.spiceflow.backend.sales.entity.RepOrderShop;
import com.spiceflow.backend.sales.entity.ShopReturn;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RepOrderMapper {
    
    public RepOrderResponse toResponse(RepOrder repOrder) {
        if (repOrder == null) return null;
        return RepOrderResponse.builder()
            .id(repOrder.getId())
            .repId(repOrder.getRep().getId())
            .repName(repOrder.getRep().getName())
            .orderDate(repOrder.getOrderDate())
            .routeArea(repOrder.getRouteArea())
            .totalGrossAmount(repOrder.getTotalGrossAmount())
            .totalReturnsValue(repOrder.getTotalReturnsValue())
            .netAmount(repOrder.getNetAmount())
            .loadingStatus(repOrder.getLoadingStatus())
            .createdAt(repOrder.getCreatedAt())
            .updatedAt(repOrder.getUpdatedAt())
            .shops(repOrder.getShops() != null ? 
                repOrder.getShops().stream().map(this::toShopResponse).collect(Collectors.toList()) : null)
            .build();
    }
    
    public RepOrderShopResponse toShopResponse(RepOrderShop shop) {
        if (shop == null) return null;
        return RepOrderShopResponse.builder()
            .id(shop.getId())
            .shopId(shop.getShop().getId())
            .shopName(shop.getShop().getName())
            .grossOrderAmount(shop.getGrossOrderAmount())
            .returnsValue(shop.getReturnsValue())
            .netAmount(shop.getNetAmount())
            .createdAt(shop.getCreatedAt())
            .items(shop.getItems() != null ? 
                shop.getItems().stream().map(this::toItemResponse).collect(Collectors.toList()) : null)
            .returns(shop.getReturns() != null ? 
                shop.getReturns().stream().map(this::toReturnResponse).collect(Collectors.toList()) : null)
            .build();
    }
    
    public RepOrderItemResponse toItemResponse(RepOrderItem item) {
        if (item == null) return null;
        return RepOrderItemResponse.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .quantity(item.getQuantity())
            .unitType(item.getUnitType())
            .rate(item.getRate())
            .grossAmount(item.getGrossAmount())
            .discountAmount(item.getDiscountAmount())
            .netAmount(item.getNetAmount())
            .isFreeItem(item.getIsFreeItem())
            .boxesNeeded(item.getBoxesNeeded())
            .build();
    }
    
    public ShopReturnResponse toReturnResponse(ShopReturn shopReturn) {
        if (shopReturn == null) return null;
        return ShopReturnResponse.builder()
            .id(shopReturn.getId())
            .productId(shopReturn.getProduct().getId())
            .productName(shopReturn.getProduct().getName())
            .productSku(shopReturn.getProduct().getSku())
            .quantity(shopReturn.getQuantity())
            .unitType(shopReturn.getUnitType())
            .creditValue(shopReturn.getCreditValue())
            .returnType(shopReturn.getReturnType())
            .status(shopReturn.getStatus())
            .createdAt(shopReturn.getCreatedAt())
            .build();
    }
}
