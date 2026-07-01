package com.spiceflow.backend.admin.dto.response;


import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantResponse {
    private Long id;
    private String businessName;
    private Long businessTypeId;
    private String businessTypeName;
    private String email;
    private String status;
    private String plan;
    private OffsetDateTime createdAt;
}
