package com.spiceflow.backend.purchase.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.common.util.UnitConversionUtil;
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
import com.spiceflow.backend.purchase.dto.request.PurchaseReturnItemRequest;
import com.spiceflow.backend.purchase.dto.response.PurchaseResponse;
import com.spiceflow.backend.purchase.entity.Purchase;
import com.spiceflow.backend.purchase.entity.PurchaseLineItem;
import com.spiceflow.backend.purchase.entity.PurchaseReturnItem;
import com.spiceflow.backend.purchase.mapper.PurchaseMapper;
import com.spiceflow.backend.purchase.repository.PurchaseLineItemRepository;
import com.spiceflow.backend.purchase.repository.PurchaseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final TenantRepository tenantRepository;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final PurchaseMapper purchaseMapper;

    @Transactional(rollbackFor = Exception.class)
    public PurchaseResponse createPurchase(Long tenantId, CreatePurchaseRequest request) {
        log.debug("Creating purchase for tenant: {}, invoice: {}", tenantId, request.invoiceNo());
        
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
            
        Supplier supplier = supplierService.getSupplierEntity(tenantId, request.supplierId());
        
        // Calculate totals
        BigDecimal totalBoxes = BigDecimal.ZERO;
        BigDecimal totalOrderValue = BigDecimal.ZERO;
        
        List<PurchaseLineItem> lineItems = new ArrayList<>();
        List<PurchaseReturnItem> returnItems = new ArrayList<>();
        
        Warehouse returnWarehouse = null;
        if (request.returnWarehouseId() != null) {
            returnWarehouse = warehouseRepository.findByIdAndTenantId(request.returnWarehouseId(), tenantId)
                .orElseThrow(() -> new BusinessRuleViolationException("Return warehouse not found"));
        }
        
        Purchase purchase = Purchase.builder()
            .tenant(tenant)
            .supplier(supplier)
            .invoiceNo(request.invoiceNo())
            .invoiceDate(request.invoiceDate())
            .orderNo(request.orderNo())
            .lcNo(request.lcNo())
            .grossWeightKg(request.grossWeightKg())
            .discountAmount(request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO)
            .returnsDeductedAmount(request.returnsDeductedAmount() != null ? request.returnsDeductedAmount() : BigDecimal.ZERO)
            .vatAmount(request.vatAmount() != null ? request.vatAmount() : BigDecimal.ZERO)
            .paymentMethod(request.paymentMethod())
            .chequeNo(request.chequeNo())
            .chequeBankName(request.chequeBankName())
            .chequeAmount(request.chequeAmount())
            .status("DRAFT")
            .notes(request.notes())
            .build();
            
        if (returnWarehouse != null) {
            purchase.setReturnWarehouse(returnWarehouse);
        }

        for (PurchaseLineItemRequest itemReq : request.lineItems()) {
            Product product = productService.getProductEntity(itemReq.productId(), tenantId);
            if (product.getSupplier() != null && !product.getSupplier().getId().equals(request.supplierId())) {
                throw new BusinessRuleViolationException("Product '" + product.getName() + "' (SKU: " + product.getSku() + ") does not belong to the selected supplier.");
            }
            
            BigDecimal calculatedSoldQuantity = itemReq.soldQuantity();

            BigDecimal amount = itemReq.amount() != null 
                ? itemReq.amount() 
                : itemReq.rate().multiply(calculatedSoldQuantity);

            
            PurchaseLineItem lineItem = PurchaseLineItem.builder()
                .tenant(tenant)
                .purchase(purchase)
                .product(product)
                .noOfBoxes(itemReq.noOfBoxes())
                .soldQuantity(calculatedSoldQuantity)
                .unitType(itemReq.unitType())
                .rate(itemReq.rate())
                .amount(amount)
                .build();
                
            lineItems.add(lineItem);
            
            totalBoxes = totalBoxes.add(itemReq.noOfBoxes());
            totalOrderValue = totalOrderValue.add(amount);
        }
        if (request.returnItems() != null) {
            for (PurchaseReturnItemRequest retReq : request.returnItems()) {
                Product product = productService.getProductEntity(retReq.productId(), tenantId);
                
                BigDecimal retAmount = retReq.amount() != null 
                    ? retReq.amount() 
                    : retReq.rate().multiply(BigDecimal.valueOf(retReq.quantity()));
                
                PurchaseReturnItem retItem = PurchaseReturnItem.builder()
                    .tenant(tenant)
                    .purchase(purchase)
                    .product(product)
                    .quantity(retReq.quantity())
                    .unitType(retReq.unitType())
                    .rate(retReq.rate())
                    .amount(retAmount)
                    .build();
                returnItems.add(retItem);
            }
        }

        purchase.setTotalBoxes(totalBoxes);
        purchase.setTotalOrderValue(totalOrderValue);
        purchase.setValueOfSupply(totalOrderValue.subtract(purchase.getDiscountAmount()).subtract(purchase.getReturnsDeductedAmount()));
        purchase.setNetAmount(purchase.getValueOfSupply().add(purchase.getVatAmount()));
        
        purchase.setLineItems(lineItems);
        purchase.setReturnItems(returnItems);
        
        Purchase savedPurchase = purchaseRepository.save(purchase);
        
        return purchaseMapper.toResponse(savedPurchase);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public PurchaseResponse updatePurchase(Long id, Long tenantId, CreatePurchaseRequest request) {
        Purchase purchase = purchaseRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
            
        if (!"DRAFT".equals(purchase.getStatus())) {
            throw new BusinessRuleViolationException("Only DRAFT purchases can be edited");
        }
        
        Supplier supplier = supplierService.getSupplierEntity(tenantId, request.supplierId());
        
        Warehouse returnWarehouse = null;
        if (request.returnWarehouseId() != null) {
            returnWarehouse = warehouseRepository.findByIdAndTenantId(request.returnWarehouseId(), tenantId)
                .orElseThrow(() -> new BusinessRuleViolationException("Return warehouse not found"));
        }
        
        purchase.setSupplier(supplier);
        purchase.setInvoiceNo(request.invoiceNo());
        purchase.setInvoiceDate(request.invoiceDate());
        purchase.setOrderNo(request.orderNo());
        purchase.setLcNo(request.lcNo());
        purchase.setGrossWeightKg(request.grossWeightKg());
        purchase.setDiscountAmount(request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO);
        purchase.setReturnsDeductedAmount(request.returnsDeductedAmount() != null ? request.returnsDeductedAmount() : BigDecimal.ZERO);
        purchase.setVatAmount(request.vatAmount() != null ? request.vatAmount() : BigDecimal.ZERO);
        purchase.setPaymentMethod(request.paymentMethod());
        purchase.setChequeNo(request.chequeNo());
        purchase.setChequeBankName(request.chequeBankName());
        purchase.setChequeAmount(request.chequeAmount());
        purchase.setNotes(request.notes());
        purchase.setReturnWarehouse(returnWarehouse);
        
        purchase.getLineItems().clear();
        purchase.getReturnItems().clear();
        
        BigDecimal totalBoxes = BigDecimal.ZERO;
        BigDecimal totalOrderValue = BigDecimal.ZERO;
        
        for (PurchaseLineItemRequest itemReq : request.lineItems()) {
            Product product = productService.getProductEntity(itemReq.productId(), tenantId);
            if (product.getSupplier() != null && !product.getSupplier().getId().equals(request.supplierId())) {
                throw new BusinessRuleViolationException("Product '" + product.getName() + "' (SKU: " + product.getSku() + ") does not belong to the selected supplier.");
            }
            
            BigDecimal calculatedSoldQuantity = itemReq.soldQuantity();
            
            BigDecimal amount = itemReq.amount() != null 
                ? itemReq.amount() 
                : itemReq.rate().multiply(calculatedSoldQuantity);
            
            PurchaseLineItem lineItem = PurchaseLineItem.builder()
                .tenant(purchase.getTenant())
                .purchase(purchase)
                .product(product)
                .noOfBoxes(itemReq.noOfBoxes())
                .soldQuantity(calculatedSoldQuantity)
                .unitType(itemReq.unitType())
                .rate(itemReq.rate())
                .amount(amount)
                .build();
                
            purchase.getLineItems().add(lineItem);
            
            totalBoxes = totalBoxes.add(itemReq.noOfBoxes());
            totalOrderValue = totalOrderValue.add(amount);
        }
        
        if (request.returnItems() != null) {
            for (PurchaseReturnItemRequest retReq : request.returnItems()) {
                Product product = productService.getProductEntity(retReq.productId(), tenantId);
                
                BigDecimal retAmount = retReq.amount() != null 
                    ? retReq.amount() 
                    : retReq.rate().multiply(BigDecimal.valueOf(retReq.quantity()));
                
                PurchaseReturnItem retItem = PurchaseReturnItem.builder()
                    .tenant(purchase.getTenant())
                    .purchase(purchase)
                    .product(product)
                    .quantity(retReq.quantity())
                    .unitType(retReq.unitType())
                    .rate(retReq.rate())
                    .amount(retAmount)
                    .build();
                purchase.getReturnItems().add(retItem);
            }
        }
        
        purchase.setTotalBoxes(totalBoxes);
        purchase.setTotalOrderValue(totalOrderValue);
        purchase.setValueOfSupply(totalOrderValue.subtract(purchase.getDiscountAmount()).subtract(purchase.getReturnsDeductedAmount()));
        purchase.setNetAmount(purchase.getValueOfSupply().add(purchase.getVatAmount()));
        
        Purchase savedPurchase = purchaseRepository.save(purchase);
        return purchaseMapper.toResponse(savedPurchase);
    }
    
    public Page<PurchaseResponse> getPurchases(Long tenantId, String invoiceNo, LocalDate date, Pageable pageable) {
        Page<Purchase> purchases;
        if (date != null) {
            purchases = purchaseRepository.findByTenantIdAndInvoiceDate(tenantId, date, pageable);
        } else if (invoiceNo != null && !invoiceNo.isBlank()) {
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
    public PurchaseResponse confirmPurchase(Long id, Long warehouseId, Long tenantId) {
        Purchase purchase = purchaseRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
            
        if (!"DRAFT".equals(purchase.getStatus())) {
            throw new BusinessRuleViolationException("Purchase is already confirmed");
        }
        
        // Prevent double-confirmation if inventory for this invoice was already processed
        List<InventoryTransaction> existingInTransactions = inventoryTransactionRepository.findByReferenceIdAndTenantId("PUR-" + purchase.getInvoiceNo(), tenantId);
        if (!existingInTransactions.isEmpty()) {
            throw new BusinessRuleViolationException("Inventory for this purchase invoice has already been added.");
        }
        
        Warehouse mainStore = warehouseRepository.findByIdAndTenantId(warehouseId, tenantId)
            .orElseThrow(() -> new BusinessRuleViolationException("Warehouse not found"));
            
        for (PurchaseLineItem lineItem : purchase.getLineItems()) {
            Optional<InventoryItem> invOpt = inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(
                lineItem.getProduct().getId(), mainStore.getId(), tenantId);
                
            // Calculate EA items directly from boxes × product packaging
            // DO NOT use soldQuantity here — it may have been manually overridden by the user
            // and represents the "sold unit" view, not raw EA count.
            Product product = lineItem.getProduct();
            int itemsPerSoldUnit = product.getItemsPerSoldUnit() != null ? product.getItemsPerSoldUnit() : 1;
            int soldUnitsPerBox = product.getSoldUnitsPerBox() != null ? product.getSoldUnitsPerBox() : 1;
            int eachQuantity = lineItem.getNoOfBoxes()
                .multiply(BigDecimal.valueOf(itemsPerSoldUnit))
                .multiply(BigDecimal.valueOf(soldUnitsPerBox))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .intValue();
                
            InventoryItem inventoryItem;
            if (invOpt.isPresent()) {
                inventoryItem = invOpt.get();
                inventoryItem.setQuantityAvailable(inventoryItem.getQuantityAvailable() + eachQuantity);
            } else {
                inventoryItem = InventoryItem.builder()
                    .tenant(purchase.getTenant())
                    .product(lineItem.getProduct())
                    .warehouse(mainStore)
                    .quantityAvailable(eachQuantity)
                    .build();
            }
            
            inventoryItem = inventoryItemRepository.save(inventoryItem);
            
            InventoryTransaction tx = InventoryTransaction.builder()
                .tenant(purchase.getTenant())
                .inventoryItem(inventoryItem)
                .transactionType("PURCHASE_IN")
                .quantity(eachQuantity)
                .referenceId("PUR-" + purchase.getInvoiceNo())
                .notes("Purchase ID: " + purchase.getId())
                .build();
                
            inventoryTransactionRepository.save(tx);
        }

        if (purchase.getReturnWarehouse() != null && purchase.getReturnItems() != null && !purchase.getReturnItems().isEmpty()) {
            Warehouse retStore = purchase.getReturnWarehouse();
            for (PurchaseReturnItem retItem : purchase.getReturnItems()) {
                InventoryItem inventoryItem = inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(
                    retItem.getProduct().getId(), retStore.getId(), tenantId)
                    .orElseThrow(() -> new BusinessRuleViolationException("Insufficient stock for return in warehouse: " + retStore.getName()));
                
                int eachReturnQuantity = UnitConversionUtil.toEachItems(retItem.getQuantity(), retItem.getUnitType());
                
                if (inventoryItem.getQuantityAvailable() < eachReturnQuantity) {
                    throw new BusinessRuleViolationException("Insufficient stock for return. Product: " + retItem.getProduct().getName());
                }
                
                inventoryItem.setQuantityAvailable(inventoryItem.getQuantityAvailable() - eachReturnQuantity);
                inventoryItemRepository.save(inventoryItem);
                
                InventoryTransaction tx = InventoryTransaction.builder()
                    .tenant(purchase.getTenant())
                    .inventoryItem(inventoryItem)
                    .transactionType("PURCHASE_RETURN_OUT")
                    .quantity(eachReturnQuantity)
                    .referenceId("PUR-RET-" + purchase.getInvoiceNo())
                    .notes("Purchase Return ID: " + purchase.getId())
                    .build();
                    
                inventoryTransactionRepository.save(tx);
            }
        }
        
        purchase.setStatus("CONFIRMED");
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    @Transactional(rollbackFor = Exception.class)
    public PurchaseResponse cancelPurchase(Long id, Long tenantId) {
        Purchase purchase = purchaseRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
            
        if (!"CONFIRMED".equals(purchase.getStatus())) {
            throw new BusinessRuleViolationException("Only CONFIRMED purchases can be cancelled/reverted to DRAFT");
        }
        
        // Find and process PURCHASE_IN transactions
        List<InventoryTransaction> inTransactions = inventoryTransactionRepository.findByReferenceIdAndTenantId("PUR-" + purchase.getInvoiceNo(), tenantId);
        for (InventoryTransaction tx : inTransactions) {
            InventoryItem item = tx.getInventoryItem();
            item.setQuantityAvailable(item.getQuantityAvailable() - tx.getQuantity());
            inventoryItemRepository.save(item);
        }
        inventoryTransactionRepository.deleteAll(inTransactions);
        
        // Find and process PURCHASE_RETURN_OUT transactions
        List<InventoryTransaction> outTransactions = inventoryTransactionRepository.findByReferenceIdAndTenantId("PUR-RET-" + purchase.getInvoiceNo(), tenantId);
        for (InventoryTransaction tx : outTransactions) {
            InventoryItem item = tx.getInventoryItem();
            item.setQuantityAvailable(item.getQuantityAvailable() + tx.getQuantity()); // add back what was deducted for returns
            inventoryItemRepository.save(item);
        }
        inventoryTransactionRepository.deleteAll(outTransactions);
        
        purchase.setStatus("DRAFT");
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePurchase(Long id, Long tenantId) {
        Purchase purchase = purchaseRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
            
        if (!"DRAFT".equals(purchase.getStatus())) {
            throw new BusinessRuleViolationException("Only DRAFT purchases can be deleted");
        }
        
        purchaseRepository.delete(purchase);
    }
}

