package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.sales.dto.response.LoadingSheetItemResponse;
import com.spiceflow.backend.sales.dto.response.LoadingSheetResponse;
import com.spiceflow.backend.sales.dto.response.LoadingSheetReturnResponse;
import com.spiceflow.backend.sales.entity.LoadingSheet;
import com.spiceflow.backend.sales.entity.LoadingSheetItem;
import com.spiceflow.backend.sales.entity.LoadingSheetReturn;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class LoadingSheetMapper {

    public LoadingSheetResponse toResponse(LoadingSheet sheet) {
        if (sheet == null) return null;
        return LoadingSheetResponse.builder()
            .id(sheet.getId())
            .repOrderId(sheet.getRepOrder().getId())
            .repId(sheet.getRepOrder().getRep().getId())
            .repName(sheet.getRepOrder().getRep().getName())
            .driverId(sheet.getDriver().getId())
            .driverName(sheet.getDriver().getName())
            .loadingDate(sheet.getLoadingDate())
            .status(sheet.getStatus())
            .createdAt(sheet.getCreatedAt())
            .updatedAt(sheet.getUpdatedAt())
            .items(sheet.getItems() != null ? 
                sheet.getItems().stream().map(this::toItemResponse).collect(Collectors.toList()) : null)
            .returns(sheet.getReturns() != null ? 
                sheet.getReturns().stream().map(this::toReturnResponse).collect(Collectors.toList()) : null)
            .build();
    }
    
    public LoadingSheetItemResponse toItemResponse(LoadingSheetItem item) {
        if (item == null) return null;
        return LoadingSheetItemResponse.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .quantityLoaded(item.getQuantityLoaded())
            .unitType(item.getUnitType())
            .build();
    }
    
    public LoadingSheetReturnResponse toReturnResponse(LoadingSheetReturn ret) {
        if (ret == null) return null;
        return LoadingSheetReturnResponse.builder()
            .id(ret.getId())
            .productId(ret.getProduct().getId())
            .productName(ret.getProduct().getName())
            .productSku(ret.getProduct().getSku())
            .quantityReturned(ret.getQuantityReturned())
            .unitType(ret.getUnitType())
            .returnType(ret.getReturnType())
            .build();
    }
}
