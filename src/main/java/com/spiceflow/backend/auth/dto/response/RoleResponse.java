package com.spiceflow.backend.auth.dto.response;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload representing a user role")
@Builder
public record RoleResponse(

    @Schema(description = "The unique identifier of the role", example = "1")
    Long id,

    @Schema(description = "The name of the role", example = "Manager")
    String name,

    @Schema(description = "Description of the role's responsibilities", example = "Can manage inventory and orders")
    String description,

    @Schema(description = "Whether this is a built-in system role that cannot be deleted", example = "false")
    Boolean isSystemRole,

    @Schema(description = "Set of permission codes granted to this role", example = "[\"INVENTORY_READ\", \"INVENTORY_WRITE\"]")
    Set<String> permissions,

    @Schema(description = "Timestamp when the role was created", example = "2026-07-02T10:00:00Z")
    OffsetDateTime createdAt


) {}