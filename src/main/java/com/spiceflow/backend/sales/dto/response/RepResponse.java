package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Builder
public record RepResponse(

    Long id,
    String employeeId,
    String name,
    String email,
    String phone,
    String area,
    LocalDate employmentDate,
    LocalDate terminationDate,
    Long assignedShopsCount,
    Boolean isActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}