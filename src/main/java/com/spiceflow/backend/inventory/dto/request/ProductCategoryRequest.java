package com.spiceflow.backend.inventory.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

@Builder
public record ProductCategoryRequest(

    
    @NotBlank(message = "Category name is required")
    String name,
    
    String description,
    
    Long parentCategoryId



) {}