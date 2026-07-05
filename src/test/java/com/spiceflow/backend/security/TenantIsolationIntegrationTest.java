package com.spiceflow.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.entity.Permission;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.RoleRepository;
import com.spiceflow.backend.auth.repository.PermissionRepository;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import com.spiceflow.backend.auth.util.JwtUtil;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.entity.Rep;
import com.spiceflow.backend.sales.repository.RepOrderRepository;
import com.spiceflow.backend.sales.repository.RepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.spiceflow.backend.auth.entity.Permission;
import com.spiceflow.backend.auth.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TenantIsolationIntegrationTest {

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
    private PermissionRepository permissionRepository;

    @Autowired
    private RepRepository repRepository;

    @Autowired
    private RepOrderRepository repOrderRepository;

    @Autowired
    private BusinessTypeRepository businessTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String tenantBToken;
    private Long tenantARepOrderId;

    @BeforeEach
    @Transactional
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        BusinessType type = businessTypeRepository.save(BusinessType.builder()
                .name("DISTRIBUTOR")
                .description("Spice Distributor")
                .build());

        // Create Tenant A
        Tenant tenantA = tenantRepository.save(Tenant.builder()
                .businessName("Tenant A Corp")
                .email("admin@tenanta.com")
                .businessType(type)
                .status("ACTIVE")
                .plan("PRO")
                .build());

        // Create Tenant A Resource (RepOrder)
        Rep repA = repRepository.save(Rep.builder()
                .tenant(tenantA)
                .name("Rep A")
                .phone("0710000001")
                .isActive(true)
                .build());

        RepOrder orderA = repOrderRepository.save(RepOrder.builder()
                .tenant(tenantA)
                .rep(repA)
                .orderDate(LocalDate.now())
                .loadingStatus("PENDING")
                .totalGrossAmount(BigDecimal.TEN)
                .totalReturnsValue(BigDecimal.ZERO)
                .netAmount(BigDecimal.TEN)
                .build());
        tenantARepOrderId = orderA.getId();

        // Create Tenant B
        Tenant tenantB = tenantRepository.save(Tenant.builder()
                .businessName("Tenant B Corp")
                .email("admin@tenantb.com")
                .businessType(type)
                .status("ACTIVE")
                .plan("PRO")
                .build());

        Permission p1 = permissionRepository.save(Permission.builder().code("ORDER_VIEW").description("View").module("SALES").build());
        Permission p2 = permissionRepository.save(Permission.builder().code("ORDER_CREATE").description("Manage").module("SALES").build());

        Role role = roleRepository.save(Role.builder()
                .tenant(tenantB)
                .name("REP_ORDER_VIEW_ROLE")
                .description("Role for Sales View")
                .isSystemRole(false)
                .permissions(java.util.Set.of(p1, p2))
                .build());

        // Create Tenant B User
        User userB = userRepository.save(User.builder()
                .tenant(tenantB)
                .email("user@tenantb.com")
                .passwordHash(passwordEncoder.encode("password"))
                .assignedRole(role)
                .build());

        // Generate JWT for Tenant B
        tenantBToken = jwtUtil.generateAccessToken(userB);
    }

    @Test
    void shouldBlockTenantBFromAccessingTenantAResource() throws Exception {
        // Tenant B attempts to fetch Tenant A's RepOrder by ID
        mockMvc.perform(get("/api/v1/sales/rep-orders/" + tenantARepOrderId)
                        .header("Authorization", "Bearer " + tenantBToken)
                        .contentType(MediaType.APPLICATION_JSON))
                // Because of findByIdAndTenantId (or Hibernate @Filter), 
                // the query will not find the record since the tenantId in the JWT does not match.
                // Thus, a ResourceNotFoundException is thrown, mapped to 404.
                .andExpect(status().isNotFound());
    }
}


