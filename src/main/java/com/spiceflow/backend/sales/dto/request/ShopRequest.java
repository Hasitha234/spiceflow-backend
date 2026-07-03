package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
@SuppressWarnings("NullAway.Init")
public class ShopRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String ownerName;
    private String phone;
    private String address;
    private String area;
    private String route;
    private Long assignedRepId;
    
    private BigDecimal outstandingLoan;
}

