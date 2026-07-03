package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.sales.dto.response.RepPerformanceResponse;
import com.spiceflow.backend.sales.dto.response.SalesSummaryResponse;
import com.spiceflow.backend.sales.dto.response.ShopOutstandingResponse;
import com.spiceflow.backend.sales.dto.response.StockStatusResponse;
import com.spiceflow.backend.sales.repository.DeliveryRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final DeliveryRepository deliveryRepository;
    private final ShopRepository shopRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Async
    public CompletableFuture<SalesSummaryResponse> getSalesSummary(Long tenantId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating sales summary for tenant {}, from {} to {}", tenantId, startDate, endDate);
        
        List<com.spiceflow.backend.sales.entity.Delivery> deliveries = deliveryRepository.findDeliveriesInDateRange(tenantId, startDate, endDate);
            
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        BigDecimal totalReturns = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;
        
        for (var delivery : deliveries) {
            totalSales = totalSales.add(delivery.getTotalSalesValue());
            totalReturns = totalReturns.add(delivery.getTotalReturnsValue());
            totalCollected = totalCollected.add(delivery.getTotalCollectedAmount());
            
            for (var shop : delivery.getShops()) {
                totalDiscounts = totalDiscounts.add(shop.getTotalDiscount());
            }
        }
        
        BigDecimal netSales = totalSales.subtract(totalReturns);
        BigDecimal creditGiven = netSales.subtract(totalCollected);
        if (creditGiven.compareTo(BigDecimal.ZERO) < 0) {
            creditGiven = BigDecimal.ZERO;
        }
        
        SalesSummaryResponse response = new SalesSummaryResponse(
            startDate,
            endDate,
            totalSales,
            totalDiscounts,
            totalReturns,
            netSales,
            totalCollected,
            creditGiven
        );
            
        log.debug("Sales summary calculated: Net Sales = {}, Collected = {}", netSales, totalCollected);
        return CompletableFuture.completedFuture(response);
    }

    public List<ShopOutstandingResponse> getShopOutstandings(Long tenantId) {
        log.info("Generating shop outstanding report for tenant {}", tenantId);
        
        List<ShopOutstandingResponse> outstandings = shopRepository.findByTenantId(tenantId, org.springframework.data.domain.Pageable.unpaged())
            .stream()
            .filter(s -> s.getOutstandingLoan() != null && s.getOutstandingLoan().compareTo(BigDecimal.ZERO) > 0)
            .map(s -> new ShopOutstandingResponse(
                s.getId(),
                s.getName(),
                s.getRoute(),
                s.getOutstandingLoan()
            ))
            .collect(Collectors.toList());
            
        log.debug("Found {} shops with outstanding balances for tenant {}", outstandings.size(), tenantId);
        return outstandings;
    }

    public List<StockStatusResponse> getStockStatus(Long tenantId) {
        log.info("Generating stock status report for tenant {}", tenantId);
        
        // This is a naive implementation that loads everything in memory.
        // In a real application, a custom JPQL query would be used.
        List<StockStatusResponse> stockStatus = inventoryItemRepository.findByTenantId(tenantId, org.springframework.data.domain.Pageable.unpaged())
            .stream()
            .collect(Collectors.groupingBy(i -> i.getProduct()))
            .entrySet().stream()
            .map(e -> {
                var product = e.getKey();
                var items = e.getValue();
                
                int mainQuantity = items.stream()
                    .filter(i -> "MAIN".equals(i.getWarehouse().getStoreType()))
                    .mapToInt(i -> i.getQuantityAvailable())
                    .sum();
                    
                int otherQuantity = items.stream()
                    .filter(i -> !"MAIN".equals(i.getWarehouse().getStoreType()))
                    .mapToInt(i -> i.getQuantityAvailable())
                    .sum();
                    
                return new StockStatusResponse(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    mainQuantity,
                    otherQuantity,
                    mainQuantity + otherQuantity
                );
            })
            .collect(Collectors.toList());
            
        log.debug("Stock status calculated for {} distinct products for tenant {}", stockStatus.size(), tenantId);
        return stockStatus;
    }

    public List<RepPerformanceResponse> getRepPerformance(Long tenantId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating rep performance report for tenant {}, from {} to {}", tenantId, startDate, endDate);
        
        List<com.spiceflow.backend.sales.entity.Delivery> deliveries = deliveryRepository.findDeliveriesInDateRange(tenantId, startDate, endDate);
            
        List<RepPerformanceResponse> performances = deliveries.stream()
            .collect(Collectors.groupingBy(d -> d.getLoadingSheet().getRepOrder().getRep()))
            .entrySet().stream()
            .map(e -> {
                var rep = e.getKey();
                var repDeliveries = e.getValue();
                
                BigDecimal totalSales = repDeliveries.stream()
                    .map(d -> d.getTotalSalesValue())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                BigDecimal totalCollected = repDeliveries.stream()
                    .map(d -> d.getTotalCollectedAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                return new RepPerformanceResponse(
                    rep.getId(),
                    rep.getName(),
                    repDeliveries.size(),
                    totalSales,
                    totalCollected,
                    null // performanceScore not calculated here
                );
            })
            .collect(Collectors.toList());
            
        log.debug("Rep performance calculated for {} reps", performances.size());
        return performances;
    }
}
