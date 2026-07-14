package com.spiceflow.backend.sales.mapper;

import com.spiceflow.backend.sales.dto.response.LoadingSheetItemResponse;
import com.spiceflow.backend.sales.dto.response.LoadingSheetResponse;
import com.spiceflow.backend.sales.dto.response.LoadingSheetReturnResponse;
import com.spiceflow.backend.sales.entity.Delivery;
import com.spiceflow.backend.sales.entity.LoadingSheet;
import com.spiceflow.backend.sales.entity.LoadingSheetItem;
import com.spiceflow.backend.sales.entity.LoadingSheetReturn;
import com.spiceflow.backend.sales.repository.DeliveryRepository;
import com.spiceflow.backend.common.mapper.CentralMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = CentralMapperConfig.class)
public abstract class LoadingSheetMapper {

    protected DeliveryRepository deliveryRepository;

    @Autowired
    public void setDeliveryRepository(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Mapping(source = "repOrder.id", target = "repOrderId")
    @Mapping(source = "repOrder.rep.id", target = "repId")
    @Mapping(source = "repOrder.rep.name", target = "repName")
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver.name", target = "driverName")
    @Mapping(source = "driver.assignedVehicle", target = "driverVehicleNo")
    @Mapping(target = "hasActiveDelivery", ignore = true)
    @Mapping(target = "activeDeliveryId", ignore = true)
    public abstract LoadingSheetResponse toResponse(LoadingSheet sheet);

    @AfterMapping
    protected void enrichDeliveryInfo(LoadingSheet sheet, @MappingTarget LoadingSheetResponse.LoadingSheetResponseBuilder builder) {
        if (sheet != null && sheet.getId() != null && deliveryRepository != null) {
            try {
                Long tenantId = sheet.getTenant() != null ? sheet.getTenant().getId() : null;
                if (tenantId != null) {
                    deliveryRepository.findFirstByLoadingSheetIdAndTenantIdOrderByIdDesc(sheet.getId(), tenantId)
                        .ifPresent(d -> {
                            builder.hasActiveDelivery(true);
                            builder.activeDeliveryId(d.getId());
                        });
                }
            } catch (Exception e) {
                // Ignore errors during delivery info enrichment to prevent 500 when listing sheets
            }
        }
    }
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    public abstract LoadingSheetItemResponse toItemResponse(LoadingSheetItem item);
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    public abstract LoadingSheetReturnResponse toReturnResponse(LoadingSheetReturn ret);
}
