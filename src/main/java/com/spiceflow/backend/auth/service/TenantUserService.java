package com.spiceflow.backend.auth.service;

import com.spiceflow.backend.admin.dto.response.UserResponse;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getDriverUsers(Long tenantId) {
        List<User> drivers = userRepository.findByTenantIdAndDeletedAtIsNull(tenantId).stream()
                .filter(u -> "DRIVER".equals(u.getUserType()))
                .collect(Collectors.toList());

        return drivers.stream()
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
    }
}
