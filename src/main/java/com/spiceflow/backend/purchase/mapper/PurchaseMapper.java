package com.spiceflow.backend.purchase.mapper;

import com.spiceflow.backend.purchase.dto.response.PurchaseLineItemResponse;
import com.spiceflow.backend.purchase.dto.response.PurchaseResponse;
import com.spiceflow.backend.purchase.entity.Purchase;
import com.spiceflow.backend.purchase.entity.PurchaseLineItem;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface PurchaseMapper {
    
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    PurchaseResponse toResponse(Purchase purchase);
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    PurchaseLineItemResponse toLineItemResponse(PurchaseLineItem lineItem);
}
