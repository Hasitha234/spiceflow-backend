package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import com.spiceflow.backend.sales.dto.response.CancelSummaryItemResponse;
import com.spiceflow.backend.sales.dto.response.CancelSummaryResponse;
import com.spiceflow.backend.sales.entity.CancelSummary;
import com.spiceflow.backend.sales.entity.CancelSummaryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface CancelSummaryMapper {

    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "rep.id", target = "repId")
    @Mapping(source = "rep.name", target = "repName")
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver.name", target = "driverName")
    CancelSummaryResponse toResponse(CancelSummary summary);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    CancelSummaryItemResponse toItemResponse(CancelSummaryItem item);
}
