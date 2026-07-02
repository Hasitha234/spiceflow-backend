package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class DriverResponse {
    private Long id;
    private String name;
    private String phone;
    private String vehicleNo;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
