package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class CreateRepOrderRequest {
    
    @NotNull(message = "Rep ID is required")
    private Long repId;
    
    @NotNull(message = "Order date is required")
    private LocalDate orderDate;
    
    private String routeArea;
    
    @Valid
    @NotNull
    private List<RepOrderShopRequest> shops;
}

