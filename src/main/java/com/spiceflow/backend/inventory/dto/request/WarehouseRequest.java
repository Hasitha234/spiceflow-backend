package com.spiceflow.backend.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WarehouseRequest {

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    private String location;

    @PositiveOrZero(message = "Capacity must be zero or positive")
    private Integer capacity;
}
