package com.spiceflow.backend.purchase.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record PurchaseResponse(

    
    Long id,
    Long supplierId,
    String supplierName,
    String invoiceNo,
    LocalDate invoiceDate,
    String orderNo,
    String lcNo,
    
    Integer totalBoxes,
    BigDecimal grossWeightKg,
    BigDecimal totalOrderValue,
    BigDecimal discountAmount,
    BigDecimal returnsDeductedAmount,
    BigDecimal valueOfSupply,
    BigDecimal vatAmount,
    BigDecimal netAmount,
    
    String paymentMethod,
    String chequeNo,
    String chequeBankName,
    BigDecimal chequeAmount,
    
    String status,
    String notes,
    
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    
    List<PurchaseLineItemResponse> lineItems,
    
    Long returnWarehouseId,
    String returnWarehouseName,
    
    List<PurchaseReturnItemResponse> returnItems



) {}