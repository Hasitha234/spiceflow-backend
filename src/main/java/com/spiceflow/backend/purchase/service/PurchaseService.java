package com.spiceflow.backend.purchase.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Supplier;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.repository.InventoryTransactionRepository;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.inventory.service.ProductService;
import com.spiceflow.backend.inventory.service.SupplierService;
import com.spiceflow.backend.purchase.dto.request.CreatePurchaseRequest;
import com.spiceflow.backend.purchase.dto.request.PurchaseLineItemRequest;
import com.spiceflow.backend.purchase.dto.response.PurchaseResponse;
import com.spiceflow.backend.purchase.entity.Purchase;
import com.spiceflow.backend.purchase.entity.PurchaseLineItem;
import com.spiceflow.backend.purchase.mapper.PurchaseMapper;
import com.spiceflow.backend.purchase.repository.PurchaseLineItemRepository;
import com.spiceflow.backend.purchase.repository.PurchaseRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseLineItemRepository purchaseLineItemRepository;
    private final TenantRepository tenantRepository;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final PurchaseMapper purchaseMapper;

    @Transactional(rollbackFor = Exception.class)
    public PurchaseResponse createPurchase(Long tenantId, CreatePurchaseRequest request) {
        log.debug("Creating purchase for tenant: {}, invoice: {}", tenantId, request.getInvoiceNo());
        
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        Supplier supplier = supplierService.getSupplierEntity(tenantId, request.getSupplierId());
        
        // Calculate totals
        int totalBoxes = 0;
        BigDecimal totalOrderValue = BigDecimal.ZERO;
        
        List<PurchaseLineItem> lineItems = new ArrayList<>();
        
        Purchase purchase = Purchase.builder()
            .tenant(tenant)
            .supplier(supplier)
            .invoiceNo(request.getInvoiceNo())
            .invoiceDate(request.getInvoiceDate())
            .orderNo(request.getOrderNo())
            .lcNo(request.getLcNo())
            .grossWeightKg(request.getGrossWeightKg())
            .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
            .returnsDeductedAmount(request.getReturnsDeductedAmount() != null ? request.getReturnsDeductedAmount() : BigDecimal.ZERO)
            .vatAmount(request.getVatAmount() != null ? request.getVatAmount() : BigDecimal.ZERO)
            .paymentMethod(request.getPaymentMethod())
            .chequeNo(request.getChequeNo())
            .chequeBankName(request.getChequeBankName())
            .chequeAmount(request.getChequeAmount())
            .status("DRAFT")
            .notes(request.getNotes())
            .build();
            
        for (PurchaseLineItemRequest itemReq : request.getLineItems()) {
            Product product = productService.getProductEntity(itemReq.getProductId(), tenantId);
            
            BigDecimal amount = itemReq.getRate().multiply(BigDecimal.valueOf(itemReq.getSoldQuantity()));
            
            PurchaseLineItem lineItem = PurchaseLineItem.builder()
                .tenant(tenant)
                .purchase(purchase)
                .product(product)
                .noOfBoxes(itemReq.getNoOfBoxes())
                .soldQuantity(itemReq.getSoldQuantity())
                .unitType(itemReq.getUnitType())
                .rate(itemReq.getRate())
                .amount(amount)
                .build();
                
            lineItems.add(lineItem);
            
            totalBoxes += itemReq.getNoOfBoxes();
            totalOrderValue = totalOrderValue.add(amount);
        }
        
        purchase.setTotalBoxes(totalBoxes);
        purchase.setTotalOrderValue(totalOrderValue);
        purchase.setValueOfSupply(totalOrderValue.subtract(purchase.getDiscountAmount()).subtract(purchase.getReturnsDeductedAmount()));
        purchase.setNetAmount(purchase.getValueOfSupply().add(purchase.getVatAmount()));
        
        purchase.setLineItems(lineItems);
        
        Purchase savedPurchase = purchaseRepository.save(purchase);
        
        return purchaseMapper.toResponse(savedPurchase);
    }
    
    public Page<PurchaseResponse> getPurchases(Long tenantId, String invoiceNo, Pageable pageable) {
        Page<Purchase> purchases;
        if (invoiceNo != null && !invoiceNo.isBlank()) {
            purchases = purchaseRepository.findByTenantIdAndInvoiceNoContainingIgnoreCase(tenantId, invoiceNo, pageable);
        } else {
            purchases = purchaseRepository.findByTenantId(tenantId, pageable);
        }
        return purchases.map(purchaseMapper::toResponse);
    }
    
    public PurchaseResponse getPurchase(Long id, Long tenantId) {
        Purchase purchase = purchaseRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        return purchaseMapper.toResponse(purchase);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public PurchaseResponse confirmPurchase(Long id, Long tenantId) {
        Purchase purchase = purchaseRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
            
        if (!"DRAFT".equals(purchase.getStatus())) {
            throw new BusinessRuleViolationException("Purchase is already confirmed");
        }
        
        // Find MAIN store
        Warehouse mainStore = warehouseRepository.findAllByTenantId(tenantId).stream()
            .filter(w -> "MAIN".equals(w.getStoreType()))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleViolationException("MAIN store not found for tenant"));
            
        for (PurchaseLineItem lineItem : purchase.getLineItems()) {
            Optional<InventoryItem> invOpt = inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(
                lineItem.getProduct().getId(), mainStore.getId(), tenantId);
                
            InventoryItem inventoryItem;
            if (invOpt.isPresent()) {
                inventoryItem = invOpt.get();
                inventoryItem.setQuantityAvailable(inventoryItem.getQuantityAvailable() + lineItem.getSoldQuantity());
            } else {
                inventoryItem = InventoryItem.builder()
                    .tenant(purchase.getTenant())
                    .product(lineItem.getProduct())
                    .warehouse(mainStore)
                    .quantityAvailable(lineItem.getSoldQuantity())
                    .build();
            }
            
            inventoryItem = inventoryItemRepository.save(inventoryItem);
            
            InventoryTransaction tx = InventoryTransaction.builder()
                .tenant(purchase.getTenant())
                .inventoryItem(inventoryItem)
                .transactionType("PURCHASE_IN")
                .quantity(lineItem.getSoldQuantity())
                .referenceId("PUR-" + purchase.getInvoiceNo())
                .notes("Purchase ID: " + purchase.getId())
                .build();
                
            inventoryTransactionRepository.save(tx);
        }
        
        purchase.setStatus("STOCK_UPDATED");
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }
}
