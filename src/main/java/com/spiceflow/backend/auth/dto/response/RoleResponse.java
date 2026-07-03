package com.spiceflow.backend.auth.dto.response;

import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "Response payload representing a user role")
public class RoleResponse {
    @Schema(description = "The unique identifier of the role", example = "1")
    private Long id;

    @Schema(description = "The name of the role", example = "Manager")
    private String name;

    @Schema(description = "Description of the role's responsibilities", example = "Can manage inventory and orders")
    private String description;

    @Schema(description = "Whether this is a built-in system role that cannot be deleted", example = "false")
    private Boolean isSystemRole;

    @Schema(description = "Set of permission codes granted to this role", example = "[\"INVENTORY_READ\", \"INVENTORY_WRITE\"]")
    private Set<String> permissions;

    @Schema(description = "Timestamp when the role was created", example = "2026-07-02T10:00:00Z")
    private OffsetDateTime createdAt;
}
