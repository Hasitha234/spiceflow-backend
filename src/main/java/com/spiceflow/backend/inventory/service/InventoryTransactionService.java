package com.spiceflow.backend.inventory.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.inventory.dto.request.InventoryTransactionRequest;
import com.spiceflow.backend.inventory.dto.response.InventoryTransactionResponse;
import com.spiceflow.backend.inventory.entity.InventoryItem;
import com.spiceflow.backend.inventory.entity.InventoryTransaction;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.inventory.repository.InventoryTransactionRepository;
import com.spiceflow.backend.inventory.mapper.InventoryTransactionMapper;
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
public class InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final TenantRepository tenantRepository;
    private final InventoryTransactionMapper transactionMapper;

    @Transactional(rollbackFor = Exception.class)
    public InventoryTransactionResponse recordTransaction(Long tenantId, InventoryTransactionRequest request) {
        log.debug("Recording transaction for tenantId: {}, inventoryItemId: {}, type: {}", 
                 tenantId, request.getInventoryItemId(), request.getTransactionType());
        try {
            Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant with ID " + tenantId + " not found"));
                
            InventoryItem item = inventoryItemRepository.findByIdAndTenantId(request.getInventoryItemId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
                
            validateTransaction(item, request.getTransactionType(), request.getQuantity());
            
            // Update inventory quantities
            updateInventoryQuantities(item, request.getTransactionType(), request.getQuantity());
            
            InventoryTransaction transaction = InventoryTransaction.builder()
                .inventoryItem(item)
                .transactionType(request.getTransactionType())
                .quantity(request.getQuantity())
                .referenceId(request.getReferenceId())
                .notes(request.getNotes())
                .tenant(tenant)
                .build();
                
            InventoryTransaction savedTx = transactionRepository.save(transaction);
            inventoryItemRepository.save(item);
            
            log.info("Successfully recorded transaction with ID: {} for tenantId: {}", savedTx.getId(), tenantId);
            return transactionMapper.toResponse(savedTx);
        } catch (BusinessRuleViolationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to record transaction for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to record transaction: " + e.getMessage());
        }
    }
    
    public Page<InventoryTransactionResponse> getTransactions(Long tenantId, Long inventoryItemId, String type, Pageable pageable) {
        log.debug("Fetching transactions for tenantId: {}, inventoryItemId: {}, type: {}", tenantId, inventoryItemId, type);
        try {
            Page<InventoryTransaction> txPage;
            if (inventoryItemId != null && type != null) {
                txPage = transactionRepository.findByInventoryItemIdAndTransactionTypeAndTenantId(inventoryItemId, type, tenantId, pageable);
            } else if (inventoryItemId != null) {
                txPage = transactionRepository.findByInventoryItemIdAndTenantId(inventoryItemId, tenantId, pageable);
            } else if (type != null) {
                txPage = transactionRepository.findByTransactionTypeAndTenantId(type, tenantId, pageable);
            } else {
                txPage = transactionRepository.findByTenantId(tenantId, pageable);
            }
            return txPage.map(transactionMapper::toResponse);
        } catch (Exception e) {
            log.error("Failed to fetch transactions for tenantId: {}", tenantId, e);
            throw new BusinessRuleViolationException("Failed to fetch transactions");
        }
    }
    
    public InventoryTransactionResponse getTransaction(Long id, Long tenantId) {
        log.debug("Fetching transaction with ID: {} for tenantId: {}", id, tenantId);
        InventoryTransaction tx = transactionRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> {
                log.error("Transaction not found with ID: {} for tenantId: {}", id, tenantId);
                return new ResourceNotFoundException("Transaction not found with id: " + id);
            });
        return transactionMapper.toResponse(tx);
    }
    
    private void validateTransaction(InventoryItem item, String type, Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleViolationException("Transaction quantity must be greater than zero");
        }
        
        switch (type) {
            case "OUT":
                if (item.getQuantityAvailable() < quantity) {
                    throw new BusinessRuleViolationException(
                        "Insufficient available quantity. Requested: " + quantity + 
                        ", Available: " + item.getQuantityAvailable());
                }
                break;
            case "RESERVE":
                if (item.getQuantityAvailable() < quantity) {
                    throw new BusinessRuleViolationException(
                        "Insufficient available quantity for reservation. Requested: " + quantity + 
                        ", Available: " + item.getQuantityAvailable());
                }
                break;
            case "UNRESERVE":
            case "SHIP_RESERVED":
                if (item.getQuantityReserved() < quantity) {
                    throw new BusinessRuleViolationException(
                        "Insufficient reserved quantity. Requested: " + quantity + 
                        ", Reserved: " + item.getQuantityReserved());
                }
                break;
        }
    }
    
    private void updateInventoryQuantities(InventoryItem item, String type, Integer quantity) {
        switch (type) {
            case "IN":
                item.setQuantityAvailable(item.getQuantityAvailable() + quantity);
                break;
            case "OUT":
                item.setQuantityAvailable(item.getQuantityAvailable() - quantity);
                break;
            case "RESERVE":
                item.setQuantityAvailable(item.getQuantityAvailable() - quantity);
                item.setQuantityReserved(item.getQuantityReserved() + quantity);
                break;
            case "UNRESERVE":
                item.setQuantityReserved(item.getQuantityReserved() - quantity);
                item.setQuantityAvailable(item.getQuantityAvailable() + quantity);
                break;
            case "SHIP_RESERVED":
                item.setQuantityReserved(item.getQuantityReserved() - quantity);
                // Total physical inventory decreases, which is already accounted for 
                // in Available when it was reserved.
                break;
            case "ADJUST_UP":
                item.setQuantityAvailable(item.getQuantityAvailable() + quantity);
                break;
            case "ADJUST_DOWN":
                if (item.getQuantityAvailable() < quantity) {
                    throw new BusinessRuleViolationException("Cannot adjust down below zero");
                }
                item.setQuantityAvailable(item.getQuantityAvailable() - quantity);
                break;
            default:
                throw new BusinessRuleViolationException("Unknown transaction type: " + type);
        }
    }
}
