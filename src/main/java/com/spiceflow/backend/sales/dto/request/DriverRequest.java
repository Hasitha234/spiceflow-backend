package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class DriverRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String phone;
    private String vehicleNo;
    
    private Boolean isActive = true;
}

