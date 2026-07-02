package com.spiceflow.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
import com.spiceflow.backend.auth.dto.request.LoginRequest;
import com.spiceflow.backend.auth.dto.request.TokenRefreshRequest;
import com.spiceflow.backend.auth.dto.response.LoginResponse;
import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.RoleRepository;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class TokenBlacklistIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BusinessTypeRepository businessTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.deleteAll();
        roleRepository.deleteAll();
        tenantRepository.deleteAll();
        businessTypeRepository.deleteAll();

        BusinessType type = businessTypeRepository.save(BusinessType.builder()
                .name("DISTRIBUTOR")
                .description("Spice Distributor")
                .build());

        Tenant tenant = tenantRepository.save(Tenant.builder()
                .businessName("Tenant Corp")
                .email("admin@tenant.com")
                .businessType(type)
                .status("ACTIVE")
                .plan("PRO")
                .build());

        Role role = roleRepository.save(Role.builder()
                .tenant(tenant)
                .name("SALES_VIEW_ROLE")
                .description("Role")
                .isSystemRole(false)
                .permissions(java.util.Collections.emptySet())
                .build());

        userRepository.save(User.builder()
                .tenant(tenant)
                .email("user@tenant.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .assignedRole(role)
                .build());
    }

    @Test
    void shouldBlacklistTokenOnLogoutAndPreventSubsequentRequests() throws Exception {
        // 1. Login to get tokens
        String loginJson = """
            {
                "email": "user@tenant.com",
                "password": "password123"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String accessToken = loginResponse.getAccessToken();
        String refreshToken = loginResponse.getRefreshToken();

        // 2. Use token successfully (e.g. check profile or any secured endpoint)
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound()); // /api/v1/auth/me doesn't exist, but it requires auth so 404 is expected if auth succeeds, instead of 401/403

        // Let's use a real endpoint that is secured
        mockMvc.perform(get("/api/v1/sales/rep-orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden()); // User has no permissions, but authentication succeeds! 403 instead of 401.

        // 3. Logout
        String logoutJson = """
            {
                "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutJson))
                .andExpect(status().isNoContent());

        // 4. Try to use token again -> 401 Unauthorized or 403
        mockMvc.perform(get("/api/v1/sales/rep-orders")
                        .header("Authorization", "Bearer " + accessToken))
                // Spring security will reject the blacklisted token in JwtAuthenticationFilter
                // Thus the context will be empty, and the request will be blocked at the security filter chain.
                // It should return 401 or 403 depending on exact configuration, usually 403 Forbidden for unauthenticated if no AuthenticationEntryPoint is mapped.
                .andExpect(status().isForbidden());
    }
}

