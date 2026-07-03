package com.spiceflow.backend.inventory.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Builder
public record WarehouseRequest(


    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name,

    String location,

    @PositiveOrZero(message = "Capacity must be zero or positive")
    Integer capacity,
    
    @Size(max = 30, message = "Store type cannot exceed 30 characters")
    String storeType,
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    String description



) {}