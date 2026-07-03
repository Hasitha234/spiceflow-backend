package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record DriverResponse(

    Long id,
    String name,
    String phone,
    String vehicleNo,
    Boolean isActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt


) {}