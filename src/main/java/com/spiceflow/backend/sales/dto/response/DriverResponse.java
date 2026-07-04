package com.spiceflow.backend.sales.dto.response;

import com.spiceflow.backend.common.enums.DriverStatus;
import com.spiceflow.backend.common.enums.LicenseClass;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Builder
public record DriverResponse(
    Long id,
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
    String defaultWarehouseName,
    String assignedVehicle,
    DriverStatus status,
    Boolean isActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}