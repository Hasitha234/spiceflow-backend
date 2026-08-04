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
import com.spiceflow.backend.sales.repository.RepOrderRepository;
import com.spiceflow.backend.purchase.repository.PurchaseRepository;
import com.spiceflow.backend.finance.repository.ExpenseRepository;
import com.spiceflow.backend.finance.entity.Expense;
import com.spiceflow.backend.sales.dto.response.MonthSummaryResponse;
import com.spiceflow.backend.sales.repository.DailyBalanceRepository;
import com.spiceflow.backend.sales.entity.DailyBalance;
import com.spiceflow.backend.sales.repository.BillRepository;
import com.spiceflow.backend.sales.entity.Bill;
import java.time.YearMonth;
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
    private final RepOrderRepository repOrderRepository;
    private final PurchaseRepository purchaseRepository;
    private final ExpenseRepository expenseRepository;
    private final DailyBalanceRepository dailyBalanceRepository;
    private final BillRepository billRepository;

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
                        if ("CHEQUE".equals(payment.getPaymentMethod())) {
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

        var dailyBalanceOpt = dailyBalanceRepository.findByTenantIdAndBalanceDate(tenantId, date);

        List<Bill> bills = billRepository.findByTenantIdAndBillDate(tenantId, date);
        int totalRepOrderBillsCount = bills.size();
        
        BigDecimal totalRepOrderBillsAmount = bills.stream()
            .map(Bill::getFinalTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate totals from bills that are not cancelled
        List<Bill> validBills = bills.stream()
            .filter(b -> !"CANCELLED".equals(b.getStatus()))
            .toList();

        totalCash = validBills.stream()
            .map(Bill::getFinalTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalCheque = validBills.stream()
            .map(Bill::getCheckCollected)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalLoan = validBills.stream()
            .map(Bill::getLoanAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cancelOrderAmount = bills.stream()
            .filter(b -> "CANCELLED".equals(b.getStatus()))
            .map(Bill::getFinalTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int cancelShopCount = (int) bills.stream()
            .filter(b -> "CANCELLED".equals(b.getStatus()))
            .count();

        List<EndOfDaySummaryResponse.DriverSummary> driverSummaries = bills.stream()
            .filter(b -> b.getDriver() != null)
            .collect(Collectors.groupingBy(b -> b.getDriver().getName()))
            .entrySet().stream()
            .map(entry -> {
                String driverName = entry.getKey();
                List<Bill> driverBills = entry.getValue();

                List<Bill> validDriverBills = driverBills.stream()
                    .filter(b -> !"CANCELLED".equals(b.getStatus()))
                    .toList();

                BigDecimal dCash = validDriverBills.stream()
                    .map(Bill::getFinalTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal dCheque = validDriverBills.stream()
                    .map(Bill::getCheckCollected)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal dLoan = validDriverBills.stream()
                    .map(Bill::getLoanAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal dCancel = driverBills.stream()
                    .filter(b -> "CANCELLED".equals(b.getStatus()))
                    .map(Bill::getFinalTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                int dCancelCount = (int) driverBills.stream()
                    .filter(b -> "CANCELLED".equals(b.getStatus()))
                    .count();

                return EndOfDaySummaryResponse.DriverSummary.builder()
                    .driverName(driverName)
                    .totalCashCollected(dCash)
                    .totalChequeAmount(dCheque)
                    .totalLoanGiven(dLoan)
                    .cancelOrderAmount(dCancel)
                    .cancelShopCount(dCancelCount)
                    .build();
            })
            .collect(Collectors.toList());

        List<EndOfDaySummaryResponse.RepOrderBillSummary> repOrderBills = bills.stream()
            .collect(Collectors.groupingBy(b -> b.getRep().getName()))
            .entrySet().stream()
            .map(entry -> {
                String repName = entry.getKey();
                List<Bill> repBills = entry.getValue();
                
                BigDecimal totalAmount = repBills.stream()
                    .map(Bill::getFinalTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                List<EndOfDaySummaryResponse.RepOrderBillShop> shops = repBills.stream()
                    .map(b -> EndOfDaySummaryResponse.RepOrderBillShop.builder()
                        .shopName(b.getShop().getName())
                        .driverName(b.getDriver() != null ? b.getDriver().getName() : "N/A")
                        .amount(b.getFinalTotal())
                        .status(b.getStatus())
                        .build())
                    .collect(Collectors.toList());
                    
                return EndOfDaySummaryResponse.RepOrderBillSummary.builder()
                    .repName(repName)
                    .orderCount(repBills.size())
                    .totalAmount(totalAmount)
                    .shops(shops)
                    .build();
            })
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
            .morningSummaryTotal(dailyBalanceOpt.map(DailyBalance::getMorningSummaryTotal).orElse(null))
            .cancelSummaryTotal(dailyBalanceOpt.map(DailyBalance::getCancelSummaryTotal).orElse(null))
            .netDispatchTotal(dailyBalanceOpt.map(DailyBalance::getNetDispatchTotal).orElse(null))
            .billsTotal(dailyBalanceOpt.map(DailyBalance::getBillsTotal).orElse(null))
            .balanceStatus(dailyBalanceOpt.map(DailyBalance::getStatus).orElse(null))
            .repOrderBills(repOrderBills)
            .driverSummaries(driverSummaries)
            .totalRepOrderBillsCount(totalRepOrderBillsCount)
            .totalRepOrderBillsAmount(totalRepOrderBillsAmount)
            .cancelOrderAmount(cancelOrderAmount)
            .cancelShopCount(cancelShopCount)
            .build();
    }

    public MonthSummaryResponse getMonthSummary(Long tenantId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<com.spiceflow.backend.sales.entity.Delivery> deliveries = deliveryRepository.findDeliveriesInDateRange(tenantId, startDate, endDate);
        List<com.spiceflow.backend.sales.entity.RepOrder> repOrders = repOrderRepository.findByTenantIdAndOrderDateBetween(tenantId, startDate, endDate);
        List<com.spiceflow.backend.purchase.entity.Purchase> purchases = purchaseRepository.findByTenantIdAndInvoiceDateBetween(tenantId, startDate, endDate);
        List<Expense> expenses = expenseRepository.findByTenantIdAndDateBetween(tenantId, startDate, endDate);

        BigDecimal totalDeliverySales = deliveries.stream()
                .map(d -> d.getTotalSalesValue() != null ? d.getTotalSalesValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRepOrderSales = repOrders.stream()
                .map(r -> r.getNetAmount() != null ? r.getNetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSales = totalDeliverySales.add(totalRepOrderSales);

        BigDecimal totalPurchases = purchases.stream()
                .map(p -> p.getNetAmount() != null ? p.getNetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenses.stream()
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalSales.subtract(totalPurchases).subtract(totalExpenses);

        List<MonthSummaryResponse.ExpenseBreakdown> expenseBreakdown = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.mapping(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .entrySet().stream()
                .map(e -> MonthSummaryResponse.ExpenseBreakdown.builder()
                        .category(e.getKey())
                        .amount(e.getValue())
                        .build())
                .collect(Collectors.toList());

        List<DailyBalance> dailyBalances = dailyBalanceRepository.findByTenantIdAndBalanceDateBetween(tenantId, startDate, endDate);
        
        BigDecimal totalMorningDispatch = dailyBalances.stream()
                .map(DailyBalance::getMorningSummaryTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal totalCancelReturns = dailyBalances.stream()
                .map(DailyBalance::getCancelSummaryTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal totalBilledAmount = dailyBalances.stream()
                .map(DailyBalance::getBillsTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return MonthSummaryResponse.builder()
                .yearMonth(yearMonth)
                .totalSalesValue(totalSales)
                .totalPurchasesValue(totalPurchases)
                .totalExpensesValue(totalExpenses)
                .netProfit(netProfit)
                .deliveryCount(deliveries.size())
                .repOrderCount(repOrders.size())
                .purchaseOrderCount(purchases.size())
                .expenseBreakdown(expenseBreakdown)
                .totalMorningDispatch(dailyBalances.isEmpty() ? null : totalMorningDispatch)
                .totalCancelReturns(dailyBalances.isEmpty() ? null : totalCancelReturns)
                .totalBilledAmount(dailyBalances.isEmpty() ? null : totalBilledAmount)
                .balancedDaysCount(dailyBalances.size())
                .build();
    }
}
