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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.spiceflow.backend.auth.util.JwtUtil;

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

    @Autowired
    private JwtUtil jwtUtil;

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
                .name("OWNER")
                .description("Admin")
                .tenant(expiredTenant)
                .isSystemRole(true)
                .build();
        roleRepository.save(role);

        User expiredUser = User.builder()
                .name("Expired User")
                .email("user@expired.com")
                .userType("DATA_ENTRY_OPERATOR")
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
                .name("OWNER")
                .description("Admin")
                .tenant(activeTenant)
                .isSystemRole(true)
                .build();
        roleRepository.save(activeRole);

        User activeUser = User.builder()
                .name("Active User")
                .email("user@active.com")
                .userType("DATA_ENTRY_OPERATOR")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .tenant(activeTenant)
                .assignedRole(activeRole)
                .build();
        userRepository.save(activeUser);

        // 3. Generate token and attempt to access protected endpoint for EXPIRED tenant -> should fail with 403
        java.util.Map<String, Object> expiredTenantMap = new java.util.HashMap<>();
        expiredTenantMap.put("id", expiredTenant.getId());
        expiredTenantMap.put("status", "EXPIRED");
        String expiredToken = jwtUtil.generateAccessToken(expiredUser, java.util.List.of(expiredTenantMap));
        
        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // 4. Generate token and attempt to access protected endpoint for ACTIVE tenant -> should succeed with 200
        java.util.Map<String, Object> activeTenantMap = new java.util.HashMap<>();
        activeTenantMap.put("id", activeTenant.getId());
        activeTenantMap.put("status", "ACTIVE");
        String activeToken = jwtUtil.generateAccessToken(activeUser, java.util.List.of(activeTenantMap));

        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + activeToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
                
        // 5. Test DISABLED tenant
        java.util.Map<String, Object> disabledTenantMap = new java.util.HashMap<>();
        disabledTenantMap.put("id", activeTenant.getId());
        disabledTenantMap.put("status", "DISABLED");
        String disabledToken = jwtUtil.generateAccessToken(activeUser, java.util.List.of(disabledTenantMap));

        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + disabledToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
                
        // 6. Test missing assignedTenants claim (should just pass through and not be blocked by lockout, relies on other auth checks)
        String noTenantToken = jwtUtil.generateAccessToken(activeUser, null);
        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + noTenantToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
