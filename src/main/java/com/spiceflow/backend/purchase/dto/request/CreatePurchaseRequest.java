package com.spiceflow.backend.purchase.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class CreatePurchaseRequest {
    
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;
    
    @NotBlank(message = "Invoice number is required")
    private String invoiceNo;
    
    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;
    
    private String orderNo;
    private String lcNo;
    
    @PositiveOrZero
    private BigDecimal grossWeightKg;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal discountAmount;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal returnsDeductedAmount;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal vatAmount;
    
    @NotBlank
    private String paymentMethod;
    
    private String chequeNo;
    private String chequeBankName;
    private BigDecimal chequeAmount;
    
    private String notes;
    
    @Valid
    @NotNull
    private List<PurchaseLineItemRequest> lineItems;
}

