package com.spiceflow.backend.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class ProductCategoryRequest {
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
    
    private Long parentCategoryId;
}

