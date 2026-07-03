package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.sales.dto.request.CreateLoadingSheetRequest;
import com.spiceflow.backend.sales.dto.response.LoadingSheetResponse;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.LoadingSheet;
import com.spiceflow.backend.sales.entity.LoadingSheetItem;
import com.spiceflow.backend.sales.entity.LoadingSheetReturn;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.entity.RepOrderItem;
import com.spiceflow.backend.sales.entity.RepOrderShop;
import com.spiceflow.backend.sales.entity.ShopReturn;
import com.spiceflow.backend.sales.mapper.LoadingSheetMapper;
import com.spiceflow.backend.sales.repository.LoadingSheetRepository;
import com.spiceflow.backend.sales.repository.RepOrderRepository;
import com.spiceflow.backend.inventory.dto.request.InventoryTransferRequest;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
import com.spiceflow.backend.inventory.service.InventoryItemService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class LoadingSheetService {

    private final LoadingSheetRepository loadingSheetRepository;
    private final RepOrderRepository repOrderRepository;
    private final TenantRepository tenantRepository;
    private final SalesMasterDataService salesMasterDataService;
    private final LoadingSheetMapper loadingSheetMapper;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemService inventoryItemService;

    @Transactional(rollbackFor = Exception.class)
    public LoadingSheetResponse createLoadingSheet(Long tenantId, CreateLoadingSheetRequest request) {
        log.info("Creating loading sheet for repOrderId: {}", request.repOrderId());
        log.debug("Tenant ID: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        RepOrder repOrder = repOrderRepository.findByIdAndTenantId(request.repOrderId(), tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("RepOrder not found"));

        if (!"DRAFT".equals(repOrder.getLoadingStatus())) {
            throw new BusinessRuleViolationException("RepOrder is already loaded or in progress");
        }

        Driver driver = salesMasterDataService.getDriverEntity(request.driverId(), tenantId);

        LoadingSheet loadingSheet = LoadingSheet.builder()
            .tenant(tenant)
            .repOrder(repOrder)
            .driver(driver)
            .loadingDate(request.loadingDate())
            .status("DRAFT")
            .build();

        // 1. Aggregate required quantities (items)
        Map<Long, LoadingSheetItem> aggregatedItems = new HashMap<>();
        for (RepOrderShop shop : repOrder.getShops()) {
            for (RepOrderItem item : shop.getItems()) {
                Long productId = item.getProduct().getId();
                LoadingSheetItem sheetItem = aggregatedItems.computeIfAbsent(productId, k -> LoadingSheetItem.builder()
                    .tenant(tenant)
                    .loadingSheet(loadingSheet)
                    .product(item.getProduct())
                    .unitType(item.getUnitType())
                    .quantityLoaded(0)
                    .build());
                sheetItem.setQuantityLoaded(sheetItem.getQuantityLoaded() + item.getQuantity());
            }
        }
        loadingSheet.setItems(new ArrayList<>(aggregatedItems.values()));

        // 2. Aggregate expected returns
        Map<String, LoadingSheetReturn> aggregatedReturns = new HashMap<>();
        for (RepOrderShop shop : repOrder.getShops()) {
            if (shop.getReturns() != null) {
                for (ShopReturn sr : shop.getReturns()) {
                    String key = sr.getProduct().getId() + "_" + sr.getReturnType();
                    LoadingSheetReturn sheetReturn = aggregatedReturns.computeIfAbsent(key, k -> LoadingSheetReturn.builder()
                        .tenant(tenant)
                        .loadingSheet(loadingSheet)
                        .product(sr.getProduct())
                        .unitType(sr.getUnitType())
                        .returnType(sr.getReturnType())
                        .quantityReturned(0)
                        .build());
                    sheetReturn.setQuantityReturned(sheetReturn.getQuantityReturned() + sr.getQuantity());
                }
            }
        }
        loadingSheet.setReturns(new ArrayList<>(aggregatedReturns.values()));

        repOrder.setLoadingStatus("IN_PROGRESS");
        repOrderRepository.save(repOrder);

        LoadingSheet savedSheet = loadingSheetRepository.save(loadingSheet);
        log.debug("Successfully created loading sheet {} with {} items and {} returns", 
            savedSheet.getId(), savedSheet.getItems().size(), savedSheet.getReturns().size());
        return loadingSheetMapper.toResponse(savedSheet);
    }
    
    public Page<LoadingSheetResponse> getLoadingSheets(Long tenantId, Pageable pageable) {
        return loadingSheetRepository.findByTenantId(tenantId, pageable)
            .map(loadingSheetMapper::toResponse);
    }

    public LoadingSheetResponse getLoadingSheet(Long id, Long tenantId) {
        LoadingSheet sheet = loadingSheetRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("LoadingSheet not found"));
        return loadingSheetMapper.toResponse(sheet);
    }

    @Transactional(rollbackFor = Exception.class)
    public LoadingSheetResponse confirmLoadingSheet(Long id, Long tenantId) {
        log.info("Confirming loading sheet: {}", id);
        LoadingSheet sheet = loadingSheetRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("LoadingSheet not found"));
            
        if (!"DRAFT".equals(sheet.getStatus())) {
            throw new BusinessRuleViolationException("Only DRAFT loading sheets can be confirmed");
        }
        
        // Find MAIN store
        Warehouse mainStore = warehouseRepository.findAllByTenantId(tenantId).stream()
            .filter(w -> "MAIN".equals(w.getStoreType()))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleViolationException("MAIN store not found for tenant"));
            
        // Find or Create VEHICLE store
        String vehicleStoreName = "Vehicle - " + sheet.getDriver().getName();
        Warehouse vehicleStore = warehouseRepository.findAllByTenantId(tenantId).stream()
            .filter(w -> "CUSTOM".equals(w.getStoreType()) && w.getName().equals(vehicleStoreName))
            .findFirst()
            .orElseGet(() -> {
                Warehouse newStore = Warehouse.builder()
                    .tenant(sheet.getTenant())
                    .name(vehicleStoreName)
                    .storeType("CUSTOM")
                    .isSystemStore(false)
                    .description("Store for vehicle/driver " + sheet.getDriver().getName())
                    .build();
                return warehouseRepository.save(newStore);
            });
            
        // Transfer items
        for (LoadingSheetItem item : sheet.getItems()) {
            if (item.getQuantityLoaded() > 0) {
                InventoryTransferRequest transferRequest = new InventoryTransferRequest(
                        mainStore.getId(),
                        vehicleStore.getId(),
                        item.getProduct().getId(),
                        item.getQuantityLoaded(),
                        "Loading Sheet " + sheet.getId()
                );
                
                inventoryItemService.transferInventory(tenantId, transferRequest);
            }
        }
        
        sheet.setStatus("CONFIRMED");
        
        RepOrder repOrder = sheet.getRepOrder();
        repOrder.setLoadingStatus("LOADED");
        repOrderRepository.save(repOrder);
        
        LoadingSheet savedSheet = loadingSheetRepository.save(sheet);
        log.debug("Successfully confirmed loading sheet {} and transferred stock to {}", savedSheet.getId(), vehicleStoreName);
        return loadingSheetMapper.toResponse(savedSheet);
    }
}
