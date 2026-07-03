package com.spiceflow.backend.purchase.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@SuppressWarnings("NullAway.Init")
public class PurchaseResponse {
    
    private Long id;
    private Long supplierId;
    private String supplierName;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private String orderNo;
    private String lcNo;
    
    private Integer totalBoxes;
    private BigDecimal grossWeightKg;
    private BigDecimal totalOrderValue;
    private BigDecimal discountAmount;
    private BigDecimal returnsDeductedAmount;
    private BigDecimal valueOfSupply;
    private BigDecimal vatAmount;
    private BigDecimal netAmount;
    
    private String paymentMethod;
    private String chequeNo;
    private String chequeBankName;
    private BigDecimal chequeAmount;
    
    private String status;
    private String notes;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    private List<PurchaseLineItemResponse> lineItems;
}

