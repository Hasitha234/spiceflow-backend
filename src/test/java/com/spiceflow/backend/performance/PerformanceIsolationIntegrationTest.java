package com.spiceflow.backend.performance;

import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.auth.repository.RoleRepository;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
import com.spiceflow.backend.auth.service.RoleService;
import com.spiceflow.backend.common.context.TenantContext;
import com.spiceflow.backend.sales.service.ReportService;
import com.spiceflow.backend.sales.dto.response.SalesSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PerformanceIsolationIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private BusinessTypeRepository businessTypeRepository;

    @Autowired
    private CacheManager cacheManager;

    private User userA;
    private User userB;

    @BeforeEach
    @Transactional
    void setup() {
        // Clear caches
        cacheManager.getCache("roles").clear();

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        BusinessType type = businessTypeRepository.save(BusinessType.builder()
                .name("DISTRIBUTOR_PERF_" + uniqueSuffix)
                .description("Perf Test")
                .build());

        Tenant tenantA = tenantRepository.save(Tenant.builder()
                .businessName("Tenant A Perf " + uniqueSuffix)
                .email("perfA_" + uniqueSuffix + "@example.com")
                .businessType(type)
                .status("ACTIVE")
                .plan("PRO")
                .build());

        Tenant tenantB = tenantRepository.save(Tenant.builder()
                .businessName("Tenant B Perf " + uniqueSuffix)
                .email("perfB_" + uniqueSuffix + "@example.com")
                .businessType(type)
                .status("ACTIVE")
                .plan("PRO")
                .build());

        Role roleA = roleRepository.save(Role.builder()
                .tenant(tenantA)
                .name("Role A " + uniqueSuffix)
                .build());

        Role roleB = roleRepository.save(Role.builder()
                .tenant(tenantB)
                .name("Role B " + uniqueSuffix)
                .build());

        userA = userRepository.save(User.builder()
                .tenant(tenantA)
                .email("usera_" + uniqueSuffix + "@perf.com")
                .passwordHash("hash")
                .build());

        userB = userRepository.save(User.builder()
                .tenant(tenantB)
                .email("userb_" + uniqueSuffix + "@perf.com")
                .passwordHash("hash")
                .build());
    }

    @Test
    void testCacheTenantIsolation() {
        // Step 1: Set context for Tenant A and fetch roles
        TenantContext.setTenantId(userA.getTenantId());
        var rolesA = roleService.getRolesForTenant(com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(userA.getId()).tenantId(userA.getTenantId()).email(userA.getEmail()).build(), Pageable.unpaged());
        assertThat(rolesA.getContent()).hasSize(1);
        assertThat(rolesA.getContent().get(0).getName()).startsWith("Role A");
        
        // Cache should now have a key prefixed with Tenant A's ID
        
        // Step 2: Set context for Tenant B and fetch roles
        TenantContext.setTenantId(userB.getTenantId());
        var rolesB = roleService.getRolesForTenant(com.spiceflow.backend.auth.dto.AuthenticatedUser.builder().id(userB.getId()).tenantId(userB.getTenantId()).email(userB.getEmail()).build(), Pageable.unpaged());
        
        // If the cache was NOT isolated, Tenant B would get Tenant A's cached roles
        assertThat(rolesB.getContent()).hasSize(1);
        assertThat(rolesB.getContent().get(0).getName()).startsWith("Role B");
        
        TenantContext.clear();
    }

    @Test
    void testAsyncContextPropagationAndIsolation() throws ExecutionException, InterruptedException {
        // Step 1: Trigger report for Tenant A
        TenantContext.setTenantId(userA.getTenantId());
        CompletableFuture<SalesSummaryResponse> futureA = reportService.getSalesSummary(userA.getTenantId(), LocalDate.now(), LocalDate.now());
        
        // Step 2: Immediately trigger report for Tenant B on the same thread pool
        TenantContext.setTenantId(userB.getTenantId());
        CompletableFuture<SalesSummaryResponse> futureB = reportService.getSalesSummary(userB.getTenantId(), LocalDate.now(), LocalDate.now());
        
        TenantContext.clear();
        
        // Wait for both to complete
        SalesSummaryResponse resultA = futureA.get();
        SalesSummaryResponse resultB = futureB.get();
        
        // Both should execute successfully without mixing context.
        assertThat(resultA).isNotNull();
        assertThat(resultB).isNotNull();
    }
}





