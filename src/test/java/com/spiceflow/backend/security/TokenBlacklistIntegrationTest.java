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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test proving that a blacklisted (logged-out) token is rejected
 * by JwtAuthenticationFilter on every subsequent request.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
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

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

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
                .name("BLACKLIST_TEST_ROLE")
                .description("Role with no permissions")
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
                .andDo(print()) // Log full response so we can see what's returned
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        // Extract tokens using JsonPath — API response is wrapped: {status, data:{accessToken, refreshToken}, timestamp}
        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = JsonPath.read(responseBody, "$.accessToken");
        String refreshToken = JsonPath.read(responseBody, "$.refreshToken");

        // 2. Prove the access token is valid: secured endpoint returns 403 (authenticated but no permission)
        //    If the token were invalid, Spring would return 401/403 via the AuthenticationEntryPoint.
        //    A 403 from @PreAuthorize confirms authentication succeeded.
        mockMvc.perform(get("/api/v1/sales/rep-orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden()); // 403 = authenticated, no permission

        // 3. Logout — this should blacklist the access token in CaffeineTokenBlacklistService
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

        // 4. Use the SAME token again — JwtAuthenticationFilter must reject it.
        //    With no authentication in the SecurityContext, the AccessDeniedHandler
        //    will still return 403. The key: if we got 403 before, and still 403 now,
        //    the token was not re-authenticated (it's still blocked).
        //    We verify this by checking /api/v1/auth/logout again — which requires auth.
        //    A second logout with the same (now-blacklisted) token should NOT succeed.
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutJson))
                // Without a valid authenticated user, the logout endpoint itself is still
                // accessible (it does not need auth to call — the auth context just won't exist).
                // What matters is the token blacklist is checked. Verify that the access token
                // truly cannot authenticate any protected endpoint:
                .andReturn(); // We just care it doesn't crash; the blacklist logic is proven below.

        // Definitive check: GET a protected endpoint with the blacklisted token.
        // Pre-logout: 403 (authenticated via valid JWT, but no REP_ORDER_VIEW permission).
        // Post-logout: 401 (blacklisted token rejected by JwtAuthenticationFilter ->
        //              SecurityContext stays empty -> AuthenticationEntryPoint returns 401).
        // The 401 here is BETTER than 403: it proves the token was truly rejected
        // by the blacklist check, not just that the user lacked permissions.
        mockMvc.perform(get("/api/v1/sales/rep-orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized()); // 401 — blacklisted token correctly rejected
    }
}

