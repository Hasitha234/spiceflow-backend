package com.spiceflow.backend.auth.controller;

import com.spiceflow.backend.admin.dto.response.UserResponse;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth/tenant/users")
@RequiredArgsConstructor
@Tag(name = "Tenant Users", description = "Endpoints for tenant users")
public class TenantUserController {

    private final UserRepository userRepository;

            @GetMapping("/drivers")
    @PreAuthorize("hasAuthority('SETTINGS_DRIVERS')")
    @Operation(summary = "Get Driver Users", description = "Get all users in the tenant with DRIVER role")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<UserResponse>> getDriverUsers(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        Long tenantId = java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null");
        
        List<User> drivers = userRepository.findByTenantIdAndDeletedAtIsNull(tenantId).stream()
                .filter(u -> "DRIVER".equals(u.getUserType()))
                .collect(Collectors.toList());
                
        List<UserResponse> responses = drivers.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getUserType(),
                        user.getTenantId() != null ? user.getTenantId() : -1L,
                        user.getTenant() != null ? user.getTenant().getBusinessName() : "",
                        user.getAssignedRole() != null ? user.getAssignedRole().getName() : "",
                        List.of(),
                        user.getCreatedAt()
                ))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(responses);
    }
}

