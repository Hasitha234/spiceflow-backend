package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

@Builder
public record DriverRequest(

    
    @NotBlank(message = "Name is required")
    String name,
    
    String phone,
    String vehicleNo,
    
    Boolean isActive



) {}