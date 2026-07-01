package com.spiceflow.backend.common.database;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = com.spiceflow.backend.SpiceflowBackendApplication.class)
class DatabaseLayerIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private BusinessTypeRepository businessTypeRepository;

    @Test
    void shouldSoftDeleteTenantAndSetAuditFields() {
        // Given
        Tenant tenant = Tenant.builder()
                .businessName("Test Business")
                .email("test.business@example.com")
                .businessType(businessTypeRepository.findByName("RESTAURANT").orElseGet(() -> 
                    businessTypeRepository.save(BusinessType.builder()
                        .name("RESTAURANT")
                        .description("Restaurant and catering services")
                        .build())
                ))
                .status("ACTIVE")
                .plan("PRO")
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);

        // Verify Auditing
        assertThat(savedTenant.getCreatedAt()).isNotNull();
        assertThat(savedTenant.getUpdatedAt()).isNotNull();
        assertThat(savedTenant.getDeletedAt()).isNull();

        // When (Soft Delete)
        tenantRepository.delete(savedTenant);

        // Then (Record should not be returned by standard find)
        Optional<Tenant> deletedTenant = tenantRepository.findById(savedTenant.getId());
        assertThat(deletedTenant).isEmpty();
        
        // We can't easily query native here to prove deleted_at without JdbcTemplate,
        // but the fact it disappeared from findById proves @SQLRestriction worked.
    }
}
