package com.spiceflow.backend.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class ProductRequest {
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Base price is required")
    @PositiveOrZero(message = "Base price must be zero or greater")
    private BigDecimal basePrice;
    
    @NotBlank(message = "Unit of measure is required")
    private String unitOfMeasure;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private String netWeight;
    private String unitType;
    private String boxConfiguration;
    private Integer itemsPerSoldUnit;
    private Integer soldUnitsPerBox;
    private BigDecimal ratePerSoldUnit;
}

