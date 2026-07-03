package com.spiceflow.backend.inventory.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Builder
public record SupplierRequest(


    @NotBlank(message = "Supplier name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name,

    @Email(message = "Invalid email format")
    @Size(max = 255)
    String contactEmail,

    @Size(max = 50)
    String contactPhone,

    String address,

    @Size(max = 100)
    String taxId



) {}