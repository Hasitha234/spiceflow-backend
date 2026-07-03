package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.service.ProductService;
import com.spiceflow.backend.sales.dto.request.CreateDeliveryRequest;
import com.spiceflow.backend.sales.dto.request.DeliveryPaymentRequest;
import com.spiceflow.backend.sales.dto.request.DeliveryShopItemRequest;
import com.spiceflow.backend.sales.dto.request.DeliveryShopReturnRequest;
import com.spiceflow.backend.sales.dto.request.RecordShopDeliveryRequest;
import com.spiceflow.backend.sales.dto.response.DeliveryResponse;
import com.spiceflow.backend.sales.dto.response.DeliveryShopResponse;
import com.spiceflow.backend.sales.entity.Delivery;
import com.spiceflow.backend.sales.entity.DeliveryPayment;
import com.spiceflow.backend.sales.entity.DeliveryShop;
import com.spiceflow.backend.sales.entity.DeliveryShopItem;
import com.spiceflow.backend.sales.entity.DeliveryShopReturn;
import com.spiceflow.backend.sales.entity.LoadingSheet;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.mapper.DeliveryMapper;
import com.spiceflow.backend.sales.repository.DeliveryRepository;
import com.spiceflow.backend.sales.repository.DeliveryShopRepository;
import com.spiceflow.backend.sales.repository.LoadingSheetRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
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
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryShopRepository deliveryShopRepository;
    private final LoadingSheetRepository loadingSheetRepository;
    private final ShopRepository shopRepository;
    private final TenantRepository tenantRepository;
    private final ProductService productService;
    private final DeliveryMapper deliveryMapper;
    
    @Transactional(rollbackFor = Exception.class)
    public DeliveryResponse createDelivery(Long tenantId, CreateDeliveryRequest request) {
        log.info("Creating delivery for loadingSheetId: {}", request.loadingSheetId());
        
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        LoadingSheet loadingSheet = loadingSheetRepository.findByIdAndTenantId(request.loadingSheetId(), tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Loading sheet not found"));
            
        if (!"CONFIRMED".equals(loadingSheet.getStatus())) {
            log.error("Failed to create delivery: Loading sheet {} is not CONFIRMED", request.loadingSheetId());
            throw new BusinessRuleViolationException("Only CONFIRMED loading sheets can be delivered");
        }
        
        Delivery delivery = Delivery.builder()
            .tenant(tenant)
            .loadingSheet(loadingSheet)
            .deliveryDate(request.deliveryDate())
            .status("IN_PROGRESS")
            .build();
            
        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.debug("Successfully created delivery {} for tenant {}", savedDelivery.getId(), tenantId);
        return deliveryMapper.toResponse(savedDelivery);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public DeliveryShopResponse recordShopDelivery(Long tenantId, Long deliveryId, Long shopId, RecordShopDeliveryRequest request) {
        log.info("Recording delivery for deliveryId: {}, shopId: {}", deliveryId, shopId);
        
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        Delivery delivery = deliveryRepository.findByIdAndTenantId(deliveryId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
            
        if (!"IN_PROGRESS".equals(delivery.getStatus())) {
            throw new BusinessRuleViolationException("Cannot record shop delivery for a non in-progress delivery");
        }
        
        Shop shop = shopRepository.findByIdAndTenantId(shopId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
            
        // Check if already recorded
        deliveryShopRepository.findByDeliveryIdAndShopIdAndTenantId(deliveryId, shopId, tenantId)
            .ifPresent(ds -> {
                throw new BusinessRuleViolationException("Delivery already recorded for this shop");
            });
            
        DeliveryShop deliveryShop = DeliveryShop.builder()
            .tenant(tenant)
            .delivery(delivery)
            .shop(shop)
            .build();
            
        BigDecimal grossBillAmount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        List<DeliveryShopItem> items = new ArrayList<>();
        for (DeliveryShopItemRequest itemReq : request.items()) {
            Product product = productService.getProductEntity(itemReq.productId(), tenantId);
            
            DeliveryShopItem item = DeliveryShopItem.builder()
                .tenant(tenant)
                .deliveryShop(deliveryShop)
                .product(product)
                .quantityDelivered(itemReq.quantityDelivered())
                .unitType(itemReq.unitType())
                .rate(itemReq.rate())
                .grossAmount(itemReq.rate().multiply(BigDecimal.valueOf(itemReq.quantityDelivered())))
                .discountAmount(itemReq.discountAmount() != null ? itemReq.discountAmount() : BigDecimal.ZERO)
                .isFreeItem(itemReq.isFreeItem() != null ? itemReq.isFreeItem() : false)
                .build();
                
            item.setNetAmount(item.getGrossAmount().subtract(item.getDiscountAmount()));
            
            grossBillAmount = grossBillAmount.add(item.getGrossAmount());
            totalDiscount = totalDiscount.add(item.getDiscountAmount());
            items.add(item);
        }
        
        BigDecimal returnsDeducted = BigDecimal.ZERO;
        List<DeliveryShopReturn> returns = new ArrayList<>();
        if (request.returns() != null) {
            for (DeliveryShopReturnRequest retReq : request.returns()) {
                Product product = productService.getProductEntity(retReq.productId(), tenantId);
                
                DeliveryShopReturn ret = DeliveryShopReturn.builder()
                    .tenant(tenant)
                    .deliveryShop(deliveryShop)
                    .product(product)
                    .quantityReturned(retReq.quantityReturned())
                    .unitType(retReq.unitType())
                    .creditValue(retReq.creditValue())
                    .returnType(retReq.returnType())
                    .build();
                    
                returnsDeducted = returnsDeducted.add(ret.getCreditValue());
                returns.add(ret);
            }
        }
        
        BigDecimal netPayable = grossBillAmount.subtract(totalDiscount).subtract(returnsDeducted);
        
        BigDecimal paidAmount = BigDecimal.ZERO;
        List<DeliveryPayment> payments = new ArrayList<>();
        if (request.payments() != null) {
            for (DeliveryPaymentRequest payReq : request.payments()) {
                DeliveryPayment payment = DeliveryPayment.builder()
                    .tenant(tenant)
                    .deliveryShop(deliveryShop)
                    .paymentMethod(payReq.paymentMethod())
                    .amount(payReq.amount())
                    .chequeNo(payReq.chequeNo())
                    .chequeBankName(payReq.chequeBankName())
                    .chequeDate(payReq.chequeDate())
                    .build();
                    
                paidAmount = paidAmount.add(payment.getAmount());
                payments.add(payment);
            }
        }
        
        BigDecimal creditAmount = netPayable.subtract(paidAmount);
        
        // Update Shop's outstanding loan if credit is given
        if (creditAmount.compareTo(BigDecimal.ZERO) > 0) {
            log.debug("Adding credit amount {} to shop {}", creditAmount, shopId);
            shop.setOutstandingLoan(shop.getOutstandingLoan().add(creditAmount));
            shopRepository.save(shop);
        }
        
        deliveryShop.setGrossBillAmount(grossBillAmount);
        deliveryShop.setTotalDiscount(totalDiscount);
        deliveryShop.setReturnsDeducted(returnsDeducted);
        deliveryShop.setNetPayable(netPayable);
        deliveryShop.setPaidAmount(paidAmount);
        deliveryShop.setCreditAmount(creditAmount);
        
        deliveryShop.setItems(items);
        deliveryShop.setReturns(returns);
        deliveryShop.setPayments(payments);
        
        delivery.getShops().add(deliveryShop);
        
        DeliveryShop savedDeliveryShop = deliveryShopRepository.save(deliveryShop);
        log.debug("Successfully recorded delivery {} for shop {}. Net Payable: {}, Paid: {}", 
            deliveryId, shopId, netPayable, paidAmount);
            
        return deliveryMapper.toShopResponse(savedDeliveryShop);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public DeliveryResponse completeDelivery(Long tenantId, Long deliveryId) {
        log.info("Completing delivery for deliveryId: {}", deliveryId);
        
        Delivery delivery = deliveryRepository.findByIdAndTenantId(deliveryId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
            
        BigDecimal totalSalesValue = BigDecimal.ZERO;
        BigDecimal totalReturnsValue = BigDecimal.ZERO;
        BigDecimal totalCollectedAmount = BigDecimal.ZERO;
        
        for (DeliveryShop shop : delivery.getShops()) {
            totalSalesValue = totalSalesValue.add(shop.getGrossBillAmount().subtract(shop.getTotalDiscount()));
            totalReturnsValue = totalReturnsValue.add(shop.getReturnsDeducted());
            totalCollectedAmount = totalCollectedAmount.add(shop.getPaidAmount());
        }
        
        delivery.setTotalSalesValue(totalSalesValue);
        delivery.setTotalReturnsValue(totalReturnsValue);
        delivery.setTotalCollectedAmount(totalCollectedAmount);
        delivery.setStatus("COMPLETED");
        
        // Here we could update inventory based on returns collected, but maybe that's done via a separate process or we can do it here.
        // For SaaS we keep it simple for now as we just track the financial value, and stock is managed via Loading and unloading/returns
        // A complete module would take returns and place them in 'CLOSED_SHOP_RETURNS' or 'EXPIRED_RETURNS' store
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.debug("Successfully completed delivery {}. Total Sales: {}, Total Collected: {}", 
            deliveryId, totalSalesValue, totalCollectedAmount);
            
        return deliveryMapper.toResponse(savedDelivery);
    }
    
    public Page<DeliveryResponse> getDeliveries(Long tenantId, Pageable pageable) {
        return deliveryRepository.findByTenantId(tenantId, pageable)
            .map(deliveryMapper::toResponse);
    }
    
    public DeliveryResponse getDelivery(Long id, Long tenantId) {
        Delivery delivery = deliveryRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        return deliveryMapper.toResponse(delivery);
    }
}
