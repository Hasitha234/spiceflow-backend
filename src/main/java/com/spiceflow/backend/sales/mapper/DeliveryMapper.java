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
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DeliveryMapper {
    
    public DeliveryResponse toResponse(Delivery delivery) {
        if (delivery == null) return null;
        return DeliveryResponse.builder()
            .id(delivery.getId())
            .loadingSheetId(delivery.getLoadingSheet().getId())
            .deliveryDate(delivery.getDeliveryDate())
            .status(delivery.getStatus())
            .totalSalesValue(delivery.getTotalSalesValue())
            .totalReturnsValue(delivery.getTotalReturnsValue())
            .totalCollectedAmount(delivery.getTotalCollectedAmount())
            .createdAt(delivery.getCreatedAt())
            .updatedAt(delivery.getUpdatedAt())
            .shops(delivery.getShops() != null ? 
                delivery.getShops().stream().map(this::toShopResponse).collect(Collectors.toList()) : null)
            .build();
    }
    
    public DeliveryShopResponse toShopResponse(DeliveryShop shop) {
        if (shop == null) return null;
        return DeliveryShopResponse.builder()
            .id(shop.getId())
            .shopId(shop.getShop().getId())
            .shopName(shop.getShop().getName())
            .grossBillAmount(shop.getGrossBillAmount())
            .totalDiscount(shop.getTotalDiscount())
            .returnsDeducted(shop.getReturnsDeducted())
            .netPayable(shop.getNetPayable())
            .paidAmount(shop.getPaidAmount())
            .creditAmount(shop.getCreditAmount())
            .createdAt(shop.getCreatedAt())
            .items(shop.getItems() != null ? 
                shop.getItems().stream().map(this::toItemResponse).collect(Collectors.toList()) : null)
            .returns(shop.getReturns() != null ? 
                shop.getReturns().stream().map(this::toReturnResponse).collect(Collectors.toList()) : null)
            .payments(shop.getPayments() != null ? 
                shop.getPayments().stream().map(this::toPaymentResponse).collect(Collectors.toList()) : null)
            .build();
    }
    
    public DeliveryShopItemResponse toItemResponse(DeliveryShopItem item) {
        if (item == null) return null;
        return DeliveryShopItemResponse.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .quantityDelivered(item.getQuantityDelivered())
            .unitType(item.getUnitType())
            .rate(item.getRate())
            .grossAmount(item.getGrossAmount())
            .discountAmount(item.getDiscountAmount())
            .netAmount(item.getNetAmount())
            .isFreeItem(item.getIsFreeItem())
            .build();
    }
    
    public DeliveryShopReturnResponse toReturnResponse(DeliveryShopReturn ret) {
        if (ret == null) return null;
        return DeliveryShopReturnResponse.builder()
            .id(ret.getId())
            .productId(ret.getProduct().getId())
            .productName(ret.getProduct().getName())
            .productSku(ret.getProduct().getSku())
            .quantityReturned(ret.getQuantityReturned())
            .unitType(ret.getUnitType())
            .creditValue(ret.getCreditValue())
            .returnType(ret.getReturnType())
            .createdAt(ret.getCreatedAt())
            .build();
    }
    
    public DeliveryPaymentResponse toPaymentResponse(DeliveryPayment payment) {
        if (payment == null) return null;
        return DeliveryPaymentResponse.builder()
            .id(payment.getId())
            .paymentMethod(payment.getPaymentMethod())
            .amount(payment.getAmount())
            .chequeNo(payment.getChequeNo())
            .chequeBankName(payment.getChequeBankName())
            .chequeDate(payment.getChequeDate())
            .createdAt(payment.getCreatedAt())
            .build();
    }
}
