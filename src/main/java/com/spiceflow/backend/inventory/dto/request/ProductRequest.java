package com.spiceflow.backend.inventory.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Builder
public record ProductRequest(

    
    @NotBlank(message = "SKU is required")
    String sku,
    
    @NotBlank(message = "Name is required")
    String name,
    
    String description,
    
    @NotNull(message = "Base price is required")
    @PositiveOrZero(message = "Base price must be zero or greater")
    BigDecimal basePrice,
    
    @NotBlank(message = "Unit of measure is required")
    String unitOfMeasure,
    
    Long categoryId,
    
    @NotNull(message = "Supplier ID is required")
    Long supplierId,

    String netWeight,
    String unitType,
    String boxConfiguration,
    Integer itemsPerSoldUnit,
    Integer soldUnitsPerBox,
    BigDecimal ratePerSoldUnit



) {}