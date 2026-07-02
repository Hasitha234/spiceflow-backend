package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class DeliveryShopResponse {
    
    private Long id;
    private Long shopId;
    private String shopName;
    
    private BigDecimal grossBillAmount;
    private BigDecimal totalDiscount;
    private BigDecimal returnsDeducted;
    private BigDecimal netPayable;
    
    private BigDecimal paidAmount;
    private BigDecimal creditAmount;
    
    private OffsetDateTime createdAt;
    
    private List<DeliveryShopItemResponse> items;
    private List<DeliveryShopReturnResponse> returns;
    private List<DeliveryPaymentResponse> payments;
}
