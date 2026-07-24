package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.service.ProductService;
import com.spiceflow.backend.inventory.service.WarehouseService;
import com.spiceflow.backend.sales.dto.request.CreateRepOrderRequest;
import com.spiceflow.backend.sales.dto.request.RepOrderItemRequest;
import com.spiceflow.backend.sales.dto.request.RepOrderShopRequest;
import com.spiceflow.backend.sales.dto.request.ShopReturnRequest;
import com.spiceflow.backend.sales.dto.response.RepOrderResponse;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.entity.RepOrderItem;
import com.spiceflow.backend.sales.entity.RepOrderShop;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.entity.ShopReturn;
import com.spiceflow.backend.sales.mapper.RepOrderMapper;
import com.spiceflow.backend.sales.repository.RepOrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepOrderService {

    private final RepOrderRepository repOrderRepository;
    private final TenantRepository tenantRepository;
    private final SalesMasterDataService salesMasterDataService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final RepOrderMapper repOrderMapper;

    @Transactional(rollbackFor = Exception.class)
    public RepOrderResponse createRepOrder(Long tenantId, CreateRepOrderRequest request) {
        log.debug("Creating rep order for repId: {} in tenantId: {}", request.repId(), tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Rep rep = salesMasterDataService.getRepEntity(request.repId(), tenantId);

        RepOrder repOrder = RepOrder.builder()
            .tenant(tenant)
            .rep(rep)
            .orderNumber(request.orderNumber())
            .orderDate(request.orderDate())
            .routeArea(request.routeArea())
            .loadingStatus("DRAFT")
            .status("DRAFT")
            .build();

        BigDecimal totalGrossAmount = BigDecimal.ZERO;
        BigDecimal totalReturnsValue = BigDecimal.ZERO;
        
        List<RepOrderShop> shops = new ArrayList<>();

        for (RepOrderShopRequest shopReq : request.shops()) {
            Shop shop = salesMasterDataService.getShopEntity(shopReq.shopId(), tenantId);

            Warehouse returnWarehouse = null;
            if (shopReq.returnWarehouseId() != null) {
                returnWarehouse = warehouseService.getWarehouseEntity(tenantId, shopReq.returnWarehouseId());
            }

            RepOrderShop orderShop = RepOrderShop.builder()
                .tenant(tenant)
                .repOrder(repOrder)
                .shop(shop)
                .discountAmount(shopReq.discountAmount() != null ? shopReq.discountAmount() : BigDecimal.ZERO)
                .skuDiscountAmount(shopReq.skuDiscountAmount() != null ? shopReq.skuDiscountAmount() : BigDecimal.ZERO)
                .reverseGrts(shopReq.reverseGrts() != null ? shopReq.reverseGrts() : BigDecimal.ZERO)
                .returns(new ArrayList<>())
                .build();
            
            if (returnWarehouse != null) {
                orderShop.setReturnWarehouse(returnWarehouse);
            }
            BigDecimal shopGross = BigDecimal.ZERO;
            BigDecimal shopReturns = BigDecimal.ZERO;

            List<RepOrderItem> items = new ArrayList<>();
            for (RepOrderItemRequest itemReq : shopReq.items()) {
                Product product = productService.getProductEntity(itemReq.productId(), tenantId);

                RepOrderItem item = RepOrderItem.builder()
                    .tenant(tenant)
                    .repOrderShop(orderShop)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitType(itemReq.unitType())
                    .rate(itemReq.rate())
                    .grossAmount(itemReq.rate().multiply(BigDecimal.valueOf(itemReq.quantity())))
                    .isFreeItem(itemReq.isFreeItem() != null ? itemReq.isFreeItem() : false)
                    .boxesNeeded(itemReq.boxesNeeded())
                    .build();
                
                item.setNetAmount(item.getGrossAmount());
                
                shopGross = shopGross.add(item.getNetAmount());
                items.add(item);
            }

            List<ShopReturn> returns = new ArrayList<>();
            if (shopReq.returns() != null) {
                for (ShopReturnRequest returnReq : shopReq.returns()) {
                    Product product = productService.getProductEntity(returnReq.productId(), tenantId);

                    ShopReturn sr = ShopReturn.builder()
                        .tenant(tenant)
                        .repOrderShop(orderShop)
                        .product(product)
                        .quantity(returnReq.quantity())
                        .unitType(returnReq.unitType())
                        .creditValue(returnReq.creditValue())
                        .returnType(returnReq.returnType())
                        .status("PENDING")
                        .build();

                    shopReturns = shopReturns.add(sr.getCreditValue());
                    returns.add(sr);
                }
            }

            orderShop.setGrossOrderAmount(shopGross);
            orderShop.setReturnsValue(shopReturns);
            // Net = Gross - (Returns - ReverseGrts) - Discount - SKU Discount
            BigDecimal effectiveReturns = shopReturns.subtract(orderShop.getReverseGrts()).max(BigDecimal.ZERO);
            orderShop.setNetAmount(shopGross.subtract(effectiveReturns).subtract(orderShop.getDiscountAmount()).subtract(orderShop.getSkuDiscountAmount()));
            
            orderShop.setItems(items);
            orderShop.setReturns(returns);
            
            shops.add(orderShop);
            
            totalGrossAmount = totalGrossAmount.add(shopGross);
            totalReturnsValue = totalReturnsValue.add(shopReturns);
            repOrder.setNetAmount(repOrder.getNetAmount() != null 
                ? repOrder.getNetAmount().add(orderShop.getNetAmount()) 
                : orderShop.getNetAmount());
        }

        repOrder.setTotalGrossAmount(totalGrossAmount);
        repOrder.setTotalReturnsValue(totalReturnsValue);
        repOrder.setShops(shops);

        RepOrder savedOrder = repOrderRepository.save(repOrder);
        return repOrderMapper.toResponse(savedOrder);
    }
    
    public Page<RepOrderResponse> getRepOrders(Long tenantId, Long repId, java.time.LocalDate date, Pageable pageable) {
        Page<RepOrder> repOrders;
        if (repId != null && date != null) {
            repOrders = repOrderRepository.findByTenantIdAndRepIdAndOrderDate(tenantId, repId, date, pageable);
        } else if (date != null) {
            repOrders = repOrderRepository.findByTenantIdAndOrderDate(tenantId, date, pageable);
        } else if (repId != null) {
            repOrders = repOrderRepository.findByTenantIdAndRepId(tenantId, repId, pageable);
        } else {
            repOrders = repOrderRepository.findByTenantId(tenantId, pageable);
        }
        return repOrders.map(repOrderMapper::toResponse);
    }

    public RepOrderResponse getRepOrder(Long id, Long tenantId) {
        RepOrder repOrder = repOrderRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("RepOrder not found"));
        return repOrderMapper.toResponse(repOrder);
    }
}
