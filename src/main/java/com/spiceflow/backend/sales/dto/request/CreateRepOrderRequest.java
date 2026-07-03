package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Builder
public record CreateRepOrderRequest(

    
    @NotNull(message = "Rep ID is required")
    Long repId,
    
    @NotNull(message = "Order date is required")
    LocalDate orderDate,
    
    String routeArea,
    
    @Valid
    @NotNull
    List<RepOrderShopRequest> shops



) {}