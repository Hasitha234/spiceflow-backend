package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.service.ProductService;
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
            .orderDate(request.orderDate())
            .routeArea(request.routeArea())
            .loadingStatus("DRAFT")
            .build();

        BigDecimal totalGrossAmount = BigDecimal.ZERO;
        BigDecimal totalReturnsValue = BigDecimal.ZERO;
        
        List<RepOrderShop> shops = new ArrayList<>();

        for (RepOrderShopRequest shopReq : request.shops()) {
            Shop shop = salesMasterDataService.getShopEntity(shopReq.shopId(), tenantId);

            RepOrderShop orderShop = RepOrderShop.builder()
                .tenant(tenant)
                .repOrder(repOrder)
                .shop(shop)
                .build();

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
                    .discountAmount(itemReq.discountAmount() != null ? itemReq.discountAmount() : BigDecimal.ZERO)
                    .isFreeItem(itemReq.isFreeItem() != null ? itemReq.isFreeItem() : false)
                    .boxesNeeded(itemReq.boxesNeeded())
                    .build();
                
                item.setNetAmount(item.getGrossAmount().subtract(item.getDiscountAmount()));
                
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
            orderShop.setNetAmount(shopGross.subtract(shopReturns));
            
            orderShop.setItems(items);
            orderShop.setReturns(returns);
            
            shops.add(orderShop);
            
            totalGrossAmount = totalGrossAmount.add(shopGross);
            totalReturnsValue = totalReturnsValue.add(shopReturns);
        }

        repOrder.setTotalGrossAmount(totalGrossAmount);
        repOrder.setTotalReturnsValue(totalReturnsValue);
        repOrder.setNetAmount(totalGrossAmount.subtract(totalReturnsValue));
        repOrder.setShops(shops);

        RepOrder savedOrder = repOrderRepository.save(repOrder);
        return repOrderMapper.toResponse(savedOrder);
    }
    
    public Page<RepOrderResponse> getRepOrders(Long tenantId, Long repId, Pageable pageable) {
        Page<RepOrder> repOrders;
        if (repId != null) {
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
