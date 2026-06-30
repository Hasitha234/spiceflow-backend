package com.spiceflow.backend.auth.dto.response;

import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isSystemRole;
    private Set<String> permissions;
    private OffsetDateTime createdAt;
}
