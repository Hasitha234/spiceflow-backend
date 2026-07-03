package com.spiceflow.backend.auth.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for creating or updating a role")
@Builder
public record RoleRequest(


    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    @Schema(description = "The name of the role", example = "Manager")
    String name,

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Description of the role's responsibilities", example = "Can manage inventory and orders")
    String description,

    @NotEmpty(message = "Role must have at least one permission")
    @Schema(description = "Set of permission codes granted to this role", example = "[\"INVENTORY_READ\", \"INVENTORY_WRITE\"]")
    Set<String> permissionCodes



) {}