package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.sales.dto.response.LoadingSheetItemResponse;
import com.spiceflow.backend.sales.dto.response.LoadingSheetResponse;
import com.spiceflow.backend.sales.dto.response.LoadingSheetReturnResponse;
import com.spiceflow.backend.sales.entity.LoadingSheet;
import com.spiceflow.backend.sales.entity.LoadingSheetItem;
import com.spiceflow.backend.sales.entity.LoadingSheetReturn;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface LoadingSheetMapper {

    @Mapping(source = "repOrder.id", target = "repOrderId")
    @Mapping(source = "repOrder.rep.id", target = "repId")
    @Mapping(source = "repOrder.rep.name", target = "repName")
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver.name", target = "driverName")
    LoadingSheetResponse toResponse(LoadingSheet sheet);
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    LoadingSheetItemResponse toItemResponse(LoadingSheetItem item);
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    LoadingSheetReturnResponse toReturnResponse(LoadingSheetReturn ret);
}
