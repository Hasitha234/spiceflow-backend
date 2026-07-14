package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.inventory.repository.InventoryItemRepository;
import com.spiceflow.backend.sales.dto.response.EndOfDaySummaryResponse;
import com.spiceflow.backend.sales.dto.response.RepPerformanceResponse;
import com.spiceflow.backend.sales.dto.response.SalesSummaryResponse;
import com.spiceflow.backend.sales.dto.response.ShopOutstandingResponse;
import com.spiceflow.backend.sales.dto.response.StockStatusResponse;
import com.spiceflow.backend.sales.repository.DeliveryRepository;
import com.spiceflow.backend.sales.repository.LoadingSheetRepository;
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
    private final LoadingSheetRepository loadingSheetRepository;

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

    public EndOfDaySummaryResponse getEndOfDaySummary(Long tenantId, LocalDate date) {
        log.info("Generating end-of-day summary for tenant {} on {}", tenantId, date);

        List<com.spiceflow.backend.sales.entity.Delivery> deliveries = deliveryRepository.findDeliveriesInDateRange(tenantId, date, date);

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalCash = BigDecimal.ZERO;
        BigDecimal totalCheque = BigDecimal.ZERO;
        BigDecimal totalLoan = BigDecimal.ZERO;
        BigDecimal totalReturns = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        int shopsVisited = 0;

        List<EndOfDaySummaryResponse.ChequeDetail> chequeDetails = new java.util.ArrayList<>();
        List<EndOfDaySummaryResponse.DeliverySummary> deliverySummaries = new java.util.ArrayList<>();

        for (var delivery : deliveries) {
            totalSales = totalSales.add(delivery.getTotalSalesValue());
            totalReturns = totalReturns.add(delivery.getTotalReturnsValue());
            shopsVisited += delivery.getShops().size();

            String driverName = "";
            if (delivery.getLoadingSheet() != null && delivery.getLoadingSheet().getDriver() != null) {
                driverName = delivery.getLoadingSheet().getDriver().getName();
            }

            for (var shop : delivery.getShops()) {
                totalDiscounts = totalDiscounts.add(shop.getTotalDiscount());

                if (shop.getPayments() != null) {
                    for (var payment : shop.getPayments()) {
                        if ("CASH".equals(payment.getPaymentMethod())) {
                            totalCash = totalCash.add(payment.getAmount());
                        } else if ("CHEQUE".equals(payment.getPaymentMethod())) {
                            totalCheque = totalCheque.add(payment.getAmount());
                            chequeDetails.add(EndOfDaySummaryResponse.ChequeDetail.builder()
                                .chequeNo(payment.getChequeNo())
                                .bankName(payment.getChequeBankName())
                                .amount(payment.getAmount())
                                .shopName(shop.getShop() != null ? shop.getShop().getName() : "")
                                .chequeDate(payment.getChequeDate())
                                .build());
                        }
                    }
                }

                totalLoan = totalLoan.add(shop.getCreditAmount());
            }

            deliverySummaries.add(EndOfDaySummaryResponse.DeliverySummary.builder()
                .deliveryId(delivery.getId())
                .driverName(driverName)
                .status(delivery.getStatus())
                .salesValue(delivery.getTotalSalesValue())
                .collectedAmount(delivery.getTotalCollectedAmount())
                .shopCount(delivery.getShops().size())
                .build());
        }

        List<EndOfDaySummaryResponse.CancelledOrderSummary> cancelledSummaries = loadingSheetRepository
            .findByTenantIdAndLoadingDateAndStatus(tenantId, date, "CANCELLED").stream()
            .map(ls -> EndOfDaySummaryResponse.CancelledOrderSummary.builder()
                .loadingSheetId(ls.getId())
                .repOrderId(ls.getRepOrder() != null ? ls.getRepOrder().getId() : null)
                .driverName(ls.getDriver() != null ? ls.getDriver().getName() : "")
                .repName(ls.getRepOrder() != null && ls.getRepOrder().getRep() != null ? ls.getRepOrder().getRep().getName() : "")
                .reason("Cancelled on " + date)
                .build())
            .collect(Collectors.toList());

        return EndOfDaySummaryResponse.builder()
            .date(date)
            .totalSalesValue(totalSales)
            .totalCashCollected(totalCash)
            .totalChequeAmount(totalCheque)
            .totalLoanGiven(totalLoan)
            .totalReturnsValue(totalReturns)
            .totalDiscounts(totalDiscounts)
            .deliveryCount(deliveries.size())
            .shopsVisited(shopsVisited)
            .chequeDetails(chequeDetails)
            .deliveries(deliverySummaries)
            .cancelledOrders(cancelledSummaries)
            .build();
    }
}
