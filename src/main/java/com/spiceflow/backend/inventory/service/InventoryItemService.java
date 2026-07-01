package com.spiceflow.backend.inventory.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.InventoryItemRequest;
import com.spiceflow.backend.inventory.dto.response.InventoryItemResponse;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.Product;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.mapper.InventoryItemMapper;
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
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final TenantRepository tenantRepository;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final InventoryItemMapper inventoryItemMapper;

    @Transactional(rollbackFor = Exception.class)
    public InventoryItemResponse createInventoryItem(Long tenantId, InventoryItemRequest request) {
        log.debug("Creating inventory item for tenantId: {}, productId: {}, warehouseId: {}", 
                 tenantId, request.getProductId(), request.getWarehouseId());
        try {
            Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant with ID " + tenantId + " not found"));
            
            // Check if product exists in this warehouse already
            inventoryItemRepository.findByProductIdAndWarehouseIdAndTenantId(
                    request.getProductId(), request.getWarehouseId(), tenantId)
                .ifPresent(i -> {
                    throw new BusinessRuleViolationException(
                        "Inventory item already exists for product " + request.getProductId() + 
                        " in warehouse " + request.getWarehouseId());
                });
                
            Product product = productService.getProductEntity(request.getProductId(), tenantId);
            Warehouse warehouse = warehouseService.getWarehouseEntity(request.getWarehouseId(), tenantId);
            
            InventoryItem item = InventoryItem.builder()
                .product(product)
                .warehouse(warehouse)
                .quantityAvailable(request.getQuantityAvailable() != null ? request.getQuantityAvailable() : 0)
                .quantityReserved(request.getQuantityReserved() != null ? request.getQuantityReserved() : 0)
                .batchNumber(request.getBatchNumber())
                .expirationDate(request.getExpirationDate())
                .tenant(tenant)
                .build();
                
            InventoryItem savedItem = inventoryItemRepository.save(item);
            log.info("Successfully created inventory item with ID: {} for tenantId: {}", savedItem.getId(), tenantId);
            return inventoryItemMapper.toResponse(savedItem);
        } catch (BusinessRuleViolationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create inventory item for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to create inventory item: " + e.getMessage());
        }
    }
    
    public Page<InventoryItemResponse> getInventoryItems(Long tenantId, Long warehouseId, Long productId, Pageable pageable) {
        log.debug("Fetching inventory items for tenantId: {}, warehouseId: {}, productId: {}", 
                 tenantId, warehouseId, productId);
        try {
            Page<InventoryItem> itemPage;
            if (warehouseId != null && productId != null) {
                itemPage = inventoryItemRepository.findByWarehouseIdAndProductIdAndTenantId(warehouseId, productId, tenantId, pageable);
            } else if (warehouseId != null) {
                itemPage = inventoryItemRepository.findByWarehouseIdAndTenantId(warehouseId, tenantId, pageable);
            } else if (productId != null) {
                itemPage = inventoryItemRepository.findByProductIdAndTenantId(productId, tenantId, pageable);
            } else {
                itemPage = inventoryItemRepository.findByTenantId(tenantId, pageable);
            }
            return itemPage.map(inventoryItemMapper::toResponse);
        } catch (Exception e) {
            log.error("Failed to fetch inventory items for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to fetch inventory items");
        }
    }
    
    public InventoryItemResponse getInventoryItem(Long id, Long tenantId) {
        log.debug("Fetching inventory item with ID: {} for tenantId: {}", id, tenantId);
        return inventoryItemMapper.toResponse(getInventoryItemEntity(id, tenantId));
    }
    
    @Transactional(rollbackFor = Exception.class)
    public InventoryItemResponse updateInventoryItem(Long id, Long tenantId, InventoryItemRequest request) {
        log.debug("Updating inventory item with ID: {} for tenantId: {}", id, tenantId);
        try {
            InventoryItem item = getInventoryItemEntity(id, tenantId);
            
            item.setQuantityAvailable(request.getQuantityAvailable() != null ? request.getQuantityAvailable() : item.getQuantityAvailable());
            item.setQuantityReserved(request.getQuantityReserved() != null ? request.getQuantityReserved() : item.getQuantityReserved());
            item.setBatchNumber(request.getBatchNumber());
            item.setExpirationDate(request.getExpirationDate());
            
            InventoryItem updatedItem = inventoryItemRepository.save(item);
            log.info("Successfully updated inventory item with ID: {} for tenantId: {}", id, tenantId);
            return inventoryItemMapper.toResponse(updatedItem);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update inventory item with ID: {} for tenantId: {}", id, tenantId, e);
            throw new BusinessRuleViolationException("Failed to update inventory item: " + e.getMessage());
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteInventoryItem(Long id, Long tenantId) {
        log.debug("Deleting inventory item with ID: {} for tenantId: {}", id, tenantId);
        try {
            InventoryItem item = getInventoryItemEntity(id, tenantId);
            if (item.getQuantityAvailable() > 0 || item.getQuantityReserved() > 0) {
                throw new BusinessRuleViolationException("Cannot delete inventory item with non-zero quantities");
            }
            inventoryItemRepository.delete(item);
            log.info("Successfully deleted inventory item with ID: {} for tenantId: {}", id, tenantId);
        } catch (BusinessRuleViolationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete inventory item with ID: {} for tenantId: {}", id, tenantId, e);
            throw new BusinessRuleViolationException("Failed to delete inventory item due to existing dependencies");
        }
    }
    
    public InventoryItem getInventoryItemEntity(Long id, Long tenantId) {
        return inventoryItemRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> {
                log.error("Inventory item not found with ID: {} for tenantId: {}", id, tenantId);
                return new ResourceNotFoundException("Inventory item not found with id: " + id);
            });
    }
}
