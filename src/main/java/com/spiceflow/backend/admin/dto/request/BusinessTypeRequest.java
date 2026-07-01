package com.spiceflow.backend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class BusinessTypeRequest {

    @Schema(description = "The name of the business type", example = "SPICE")
    @NotBlank(message = "Business type name is required")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String name;

    @Schema(description = "Description of the business type", example = "Spice manufacturing and processing")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}
