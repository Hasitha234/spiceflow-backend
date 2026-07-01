package com.spiceflow.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Data;

@Data
public class RoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @NotEmpty(message = "Role must have at least one permission")
    private Set<String> permissionCodes;
}
