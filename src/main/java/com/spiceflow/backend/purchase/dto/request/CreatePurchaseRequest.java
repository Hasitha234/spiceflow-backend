package com.spiceflow.backend.purchase.dto.request;
import lombok.Builder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record CreatePurchaseRequest(

    
    @NotNull(message = "Supplier ID is required")
    Long supplierId,
    
    @NotBlank(message = "Invoice number is required")
    String invoiceNo,
    
    @NotNull(message = "Invoice date is required")
    LocalDate invoiceDate,
    
    String orderNo,
    String lcNo,
    
    @PositiveOrZero
    BigDecimal grossWeightKg,
    
    @NotNull
    @PositiveOrZero
    BigDecimal discountAmount,
    
    @NotNull
    @PositiveOrZero
    BigDecimal returnsDeductedAmount,
    
    @NotNull
    @PositiveOrZero
    BigDecimal vatAmount,
    
    @NotBlank
    String paymentMethod,
    
    String chequeNo,
    String chequeBankName,
    BigDecimal chequeAmount,
    
    String notes,
    
    @Valid
    @NotNull
    List<PurchaseLineItemRequest> lineItems,

    Long returnWarehouseId,
    
    @Valid
    List<PurchaseReturnItemRequest> returnItems



) {}