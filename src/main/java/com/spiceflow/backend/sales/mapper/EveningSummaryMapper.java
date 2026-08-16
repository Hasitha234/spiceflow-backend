package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import com.spiceflow.backend.sales.dto.response.EveningSummaryItemResponse;
import com.spiceflow.backend.sales.dto.response.EveningSummaryResponse;
import com.spiceflow.backend.sales.entity.EveningSummary;
import com.spiceflow.backend.sales.entity.EveningSummaryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface EveningSummaryMapper {

    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "rep.id", target = "repId")
    @Mapping(source = "rep.name", target = "repName")
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver.name", target = "driverName")
    @Mapping(source = "deductionWarehouse.id", target = "deductionWarehouseId")
    @Mapping(source = "deductionWarehouse.name", target = "deductionWarehouseName")
    EveningSummaryResponse toResponse(EveningSummary summary);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    EveningSummaryItemResponse toItemResponse(EveningSummaryItem item);
}
