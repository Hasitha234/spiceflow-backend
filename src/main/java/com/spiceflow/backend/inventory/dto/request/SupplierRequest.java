package com.spiceflow.backend.inventory.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String contactEmail;

    @Size(max = 50)
    private String contactPhone;

    private String address;

    @Size(max = 100)
    private String taxId;
}
