package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Builder
public record RepRequest(

    String employeeId,

    @NotBlank(message = "Name is required")
    String name,

    @Email(message = "Invalid email format")
    String email,

    String phone,
    String area,

    LocalDate employmentDate,
    LocalDate terminationDate,

    Boolean isActive
) {}