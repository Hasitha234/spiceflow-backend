package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String phone;
    private String area;
    
    private Boolean isActive = true;
}
