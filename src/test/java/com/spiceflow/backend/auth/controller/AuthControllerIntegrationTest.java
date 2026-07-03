package com.spiceflow.backend.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spiceflow.backend.auth.dto.request.LoginRequest;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;

@SpringBootTest
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private com.spiceflow.backend.admin.repository.BusinessTypeRepository businessTypeRepository;

    @Autowired
    private com.spiceflow.backend.auth.repository.TenantRepository tenantRepository;

    @Autowired
    private com.spiceflow.backend.auth.repository.RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        if (userRepository.findByEmailAndDeletedAtIsNull("test@spiceflow.com").isEmpty()) {
            com.spiceflow.backend.admin.entity.BusinessType type = businessTypeRepository.save(
                com.spiceflow.backend.admin.entity.BusinessType.builder()
                    .name("DISTRIBUTOR_TEST")
                    .description("Test Type")
                    .build());

            com.spiceflow.backend.auth.entity.Tenant tenant = tenantRepository.save(
                com.spiceflow.backend.auth.entity.Tenant.builder()
                    .businessName("Test Corp")
                    .email("admin@testcorp.com")
                    .businessType(type)
                    .status("ACTIVE")
                    .plan("PRO")
                    .build());

            com.spiceflow.backend.auth.entity.Role role = roleRepository.save(
                com.spiceflow.backend.auth.entity.Role.builder()
                    .tenant(tenant)
                    .name("ADMIN")
                    .description("Admin role")
                    .isSystemRole(true)
                    .build());

            com.spiceflow.backend.auth.entity.User user = com.spiceflow.backend.auth.entity.User.builder()
                .tenant(tenant)
                .email("test@spiceflow.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .assignedRole(role)
                .build();
            userRepository.save(user);
        }
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        String jsonPayload = "{ \"email\": \"test@spiceflow.com\", \"password\": \"password123\" }";

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        String jsonPayload = "{ \"email\": \"test@spiceflow.com\", \"password\": \"wrongpassword\" }";

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isUnauthorized()); // Invalid credentials map to 401
    }

    @Test
    void shouldRejectMissingEmail() throws Exception {
        String jsonPayload = "{ \"password\": \"password123\" }";

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest()); // JSR-303 Validation fails
    }
}
