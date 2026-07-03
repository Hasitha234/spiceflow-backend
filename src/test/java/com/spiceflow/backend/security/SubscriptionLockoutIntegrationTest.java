package com.spiceflow.backend.security;

import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionLockoutIntegrationTest {

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldBlockLoginWhenTenantIsExpired() throws Exception {
        // 1. Setup an EXPIRED tenant
        BusinessType type = BusinessType.builder().name("Retail").description("Retail Shop").build();
        businessTypeRepository.save(type);

        Tenant expiredTenant = Tenant.builder()
                .businessName("Expired Corp")
                .email("expired@corp.com")
                .status("EXPIRED")
                .plan("PREMIUM")
                .businessType(type)
                .build();
        tenantRepository.save(expiredTenant);

        Role role = Role.builder()
                .name("Admin")
                .description("Admin")
                .tenant(expiredTenant)
                .isSystemRole(true)
                .build();
        roleRepository.save(role);

        User expiredUser = User.builder()
                .email("user@expired.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .tenant(expiredTenant)
                .assignedRole(role)
                .build();
        userRepository.save(expiredUser);

        // 2. Setup an ACTIVE tenant
        Tenant activeTenant = Tenant.builder()
                .businessName("Active Corp")
                .email("active@corp.com")
                .status("ACTIVE")
                .plan("PREMIUM")
                .businessType(type)
                .build();
        tenantRepository.save(activeTenant);

        Role activeRole = Role.builder()
                .name("Admin")
                .description("Admin")
                .tenant(activeTenant)
                .isSystemRole(true)
                .build();
        roleRepository.save(activeRole);

        User activeUser = User.builder()
                .email("user@active.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .tenant(activeTenant)
                .assignedRole(activeRole)
                .build();
        userRepository.save(activeUser);

        // 3. Attempt Login for EXPIRED tenant -> should fail with 403 (Access Denied mapped in GlobalExceptionHandler)
        String expiredLoginJson = """
                {
                  "email": "user@expired.com",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expiredLoginJson))
                .andExpect(status().isForbidden());

        // 4. Attempt Login for ACTIVE tenant -> should succeed with 200
        String activeLoginJson = """
                {
                  "email": "user@active.com",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activeLoginJson))
                .andExpect(status().isOk());
    }
}
