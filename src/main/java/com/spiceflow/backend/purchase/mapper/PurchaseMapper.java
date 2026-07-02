package com.spiceflow.backend.purchase.mapper;

import com.spiceflow.backend.purchase.dto.response.PurchaseLineItemResponse;
import com.spiceflow.backend.purchase.dto.response.PurchaseResponse;
import com.spiceflow.backend.purchase.entity.Purchase;
import com.spiceflow.backend.purchase.entity.PurchaseLineItem;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PurchaseMapper {
    
    public PurchaseResponse toResponse(Purchase purchase) {
        if (purchase == null) {
            return null;
        }
        
        return PurchaseResponse.builder()
            .id(purchase.getId())
            .supplierId(purchase.getSupplier().getId())
            .supplierName(purchase.getSupplier().getName())
            .invoiceNo(purchase.getInvoiceNo())
            .invoiceDate(purchase.getInvoiceDate())
            .orderNo(purchase.getOrderNo())
            .lcNo(purchase.getLcNo())
            .totalBoxes(purchase.getTotalBoxes())
            .grossWeightKg(purchase.getGrossWeightKg())
            .totalOrderValue(purchase.getTotalOrderValue())
            .discountAmount(purchase.getDiscountAmount())
            .returnsDeductedAmount(purchase.getReturnsDeductedAmount())
            .valueOfSupply(purchase.getValueOfSupply())
            .vatAmount(purchase.getVatAmount())
            .netAmount(purchase.getNetAmount())
            .paymentMethod(purchase.getPaymentMethod())
            .chequeNo(purchase.getChequeNo())
            .chequeBankName(purchase.getChequeBankName())
            .chequeAmount(purchase.getChequeAmount())
            .status(purchase.getStatus())
            .notes(purchase.getNotes())
            .createdAt(purchase.getCreatedAt())
            .updatedAt(purchase.getUpdatedAt())
            .lineItems(purchase.getLineItems() != null ? 
                purchase.getLineItems().stream().map(this::toLineItemResponse).collect(Collectors.toList()) : null)
            .build();
    }
    
    public PurchaseLineItemResponse toLineItemResponse(PurchaseLineItem lineItem) {
        if (lineItem == null) {
            return null;
        }
        
        return PurchaseLineItemResponse.builder()
            .id(lineItem.getId())
            .productId(lineItem.getProduct().getId())
            .productName(lineItem.getProduct().getName())
            .productSku(lineItem.getProduct().getSku())
            .noOfBoxes(lineItem.getNoOfBoxes())
            .soldQuantity(lineItem.getSoldQuantity())
            .unitType(lineItem.getUnitType())
            .rate(lineItem.getRate())
            .amount(lineItem.getAmount())
            .build();
    }
}
