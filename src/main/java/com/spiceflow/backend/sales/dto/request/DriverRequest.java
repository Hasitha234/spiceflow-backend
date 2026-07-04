package com.spiceflow.backend.sales.dto.request;

import com.spiceflow.backend.common.enums.DriverStatus;
import com.spiceflow.backend.common.enums.LicenseClass;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DriverRequest(
    @NotBlank(message = "Name is required")
    String name,
    String employeeId,
    String email,
    String phone,
    LocalDate employmentDate,
    LocalDate terminationDate,
    String licenseNumber,
    LicenseClass licenseClass,
    LocalDate licenseExpiry,
    Long defaultWarehouseId,
    String assignedVehicle,
    DriverStatus status,
    Boolean isActive
) {}