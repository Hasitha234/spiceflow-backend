package com.spiceflow.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request payload for creating or updating a role")
@SuppressWarnings("NullAway.Init")
public class RoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    @Schema(description = "The name of the role", example = "Manager")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Description of the role's responsibilities", example = "Can manage inventory and orders")
    private String description;

    @NotEmpty(message = "Role must have at least one permission")
    @Schema(description = "Set of permission codes granted to this role", example = "[\"INVENTORY_READ\", \"INVENTORY_WRITE\"]")
    private Set<String> permissionCodes;
}

