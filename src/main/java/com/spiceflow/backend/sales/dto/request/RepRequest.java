package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

@Builder
public record RepRequest(

    
    @NotBlank(message = "Name is required")
    String name,
    
    String phone,
    String area,
    
    Boolean isActive



) {}