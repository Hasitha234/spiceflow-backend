package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.sales.dto.request.BillCollectionRequest;
import com.spiceflow.backend.sales.dto.request.BillRequest;
import com.spiceflow.backend.sales.dto.response.BillResponse;
import com.spiceflow.backend.sales.entity.Bill;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.repository.BillRepository;
import com.spiceflow.backend.sales.repository.DriverRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final TenantRepository tenantRepository;
    private final RepRepository repRepository;
    private final DriverRepository driverRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public BillResponse createBill(Long tenantId, BillRequest request) {
        if (billRepository.existsByTenantIdAndShopIdAndBillDate(tenantId, request.shopId(), request.billDate())) {
            throw new IllegalArgumentException("A bill already exists for this shop on this date.");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Rep rep = repRepository.findByIdAndTenantId(request.repId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));

        Driver driver = null;
        if (request.driverId() != null) {
            driver = driverRepository.findByIdAndTenantId(request.driverId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        }

        Shop shop = shopRepository.findByIdAndTenantId(request.shopId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        BigDecimal reverseGrts = request.reverseGrts() != null ? request.reverseGrts() : BigDecimal.ZERO;

        BigDecimal finalTotal = request.netTotal().add(reverseGrts)
                .subtract(request.discount()).subtract(request.skuDiscount())
                .subtract(request.returnAmount());

        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final total cannot be negative");
        }

        Bill bill = Bill.builder()
                .tenant(tenant)
                .rep(rep)
                .driver(driver)
                .shop(shop)
                .billNumber(generateBillNumber(tenantId))
                .billDate(request.billDate())
                .netTotal(request.netTotal())
                .reverseGrts(reverseGrts)
                .freeItemsValue(request.freeItemsValue())
                .discount(request.discount())
                .skuDiscount(request.skuDiscount())
                .returnAmount(request.returnAmount())
                .finalTotal(finalTotal)
                .status("PENDING")
                .build();

        Bill savedBill = billRepository.save(bill);
        return mapToResponse(savedBill);
    }
    @Transactional
    public BillResponse updateBill(Long tenantId, Long id, BillRequest request) {
        Bill bill = billRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (!"PENDING".equals(bill.getStatus())) {
            throw new IllegalArgumentException("Only PENDING bills can be updated.");
        }

        // Check if date or shop changed and another bill exists
        if ((!bill.getBillDate().equals(request.billDate()) || !bill.getShop().getId().equals(request.shopId())) &&
            billRepository.existsByTenantIdAndShopIdAndBillDate(tenantId, request.shopId(), request.billDate())) {
            throw new IllegalArgumentException("A bill already exists for this shop on this date.");
        }

        Rep rep = repRepository.findByIdAndTenantId(request.repId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Rep not found"));

        Driver driver = null;
        if (request.driverId() != null) {
            driver = driverRepository.findByIdAndTenantId(request.driverId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        }

        Shop shop = shopRepository.findByIdAndTenantId(request.shopId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        BigDecimal reverseGrts = request.reverseGrts() != null ? request.reverseGrts() : BigDecimal.ZERO;

        BigDecimal finalTotal = request.netTotal().add(reverseGrts)
                .subtract(request.discount()).subtract(request.skuDiscount())
                .subtract(request.returnAmount());

        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final total cannot be negative");
        }

        bill.setRep(rep);
        bill.setDriver(driver);
        bill.setShop(shop);
        bill.setBillDate(request.billDate());
        bill.setNetTotal(request.netTotal());
        bill.setReverseGrts(reverseGrts);
        bill.setFreeItemsValue(request.freeItemsValue());
        bill.setDiscount(request.discount());
        bill.setSkuDiscount(request.skuDiscount());
        bill.setReturnAmount(request.returnAmount());
        bill.setFinalTotal(finalTotal);

        Bill savedBill = billRepository.save(bill);
        return mapToResponse(savedBill);
    }

    @Transactional
    public BillResponse collectBill(Long tenantId, Long billId, BillCollectionRequest request) {
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (!"PENDING".equals(bill.getStatus())) {
            throw new IllegalArgumentException("Can only collect pending bills.");
        }

        BigDecimal totalCollected = request.cashCollected().add(request.checkCollected()).add(request.loanAmount());
        
        if (totalCollected.compareTo(bill.getFinalTotal()) != 0) {
            throw new IllegalArgumentException("Collected amounts must exactly equal the final total.");
        }

        if (request.loanAmount().compareTo(BigDecimal.ZERO) > 0 && request.loanDueDate() == null) {
            throw new IllegalArgumentException("Loan due date is required when a loan is given.");
        }

        bill.setCashCollected(request.cashCollected());
        bill.setCheckCollected(request.checkCollected());
        bill.setLoanAmount(request.loanAmount());
        bill.setLoanDueDate(request.loanDueDate());
        
        if (request.loanAmount().compareTo(BigDecimal.ZERO) > 0) {
            bill.setLoanStatus("UNPAID");
            
            // Add loan amount to shop's outstanding balance
            Shop shop = bill.getShop();
            shop.setOutstandingLoan(shop.getOutstandingLoan().add(request.loanAmount()));
            shopRepository.save(shop);
        } else {
            bill.setLoanStatus("NONE");
        }
        
        bill.setStatus("COLLECTED");
        
        return mapToResponse(billRepository.save(bill));
    }

    @Transactional
    public BillResponse cancelBill(Long tenantId, Long billId) {
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if ("CANCELLED".equals(bill.getStatus())) {
            return mapToResponse(bill);
        }

        if ("COLLECTED".equals(bill.getStatus()) && bill.getLoanAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Revert loan from shop's outstanding balance
            Shop shop = bill.getShop();
            shop.setOutstandingLoan(shop.getOutstandingLoan().subtract(bill.getLoanAmount()));
            shopRepository.save(shop);
        }

        bill.setStatus("CANCELLED");
        return mapToResponse(billRepository.save(bill));
    }

    @Transactional(readOnly = true)
    public Page<BillResponse> getBills(Long tenantId, LocalDate billDate, Long repId, Long shopId, String status, String search, Pageable pageable) {
        return billRepository.findBillsWithFilters(tenantId, billDate, repId, shopId, status, search, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public BillResponse getBillById(Long tenantId, Long billId) {
        return billRepository.findByIdAndTenantId(billId, tenantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
    }

    private String generateBillNumber(Long tenantId) {
        return billRepository.findFirstByTenantIdOrderByCreatedAtDesc(tenantId)
                .map(b -> {
                    String lastNumber = b.getBillNumber();
                    try {
                        int num = Integer.parseInt(lastNumber.replace("BL-", ""));
                        return String.format("BL-%04d", num + 1);
                    } catch (Exception e) {
                        return "BL-0001";
                    }
                })
                .orElse("BL-0001");
    }

    private BillResponse mapToResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .billDate(bill.getBillDate())
                .repId(bill.getRep().getId())
                .repName(bill.getRep().getName())
                .driverId(bill.getDriver() != null ? bill.getDriver().getId() : null)
                .driverName(bill.getDriver() != null ? bill.getDriver().getName() : null)
                .shopId(bill.getShop().getId())
                .shopName(bill.getShop().getName())
                .netTotal(bill.getNetTotal())
                .reverseGrts(bill.getReverseGrts())
                .freeItemsValue(bill.getFreeItemsValue())
                .discount(bill.getDiscount())
                .skuDiscount(bill.getSkuDiscount())
                .returnAmount(bill.getReturnAmount())
                .finalTotal(bill.getFinalTotal())
                .status(bill.getStatus())
                .cashCollected(bill.getCashCollected())
                .checkCollected(bill.getCheckCollected())
                .loanAmount(bill.getLoanAmount())
                .loanDueDate(bill.getLoanDueDate())
                .loanStatus(bill.getLoanStatus())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}
