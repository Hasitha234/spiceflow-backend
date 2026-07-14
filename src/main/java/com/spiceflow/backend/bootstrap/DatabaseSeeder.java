package com.spiceflow.backend.bootstrap;

import com.spiceflow.backend.admin.entity.PlatformAdmin;
import com.spiceflow.backend.admin.repository.PlatformAdminRepository;
import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.auth.repository.PermissionRepository;
import com.spiceflow.backend.auth.repository.RoleRepository;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.util.HashSet;

/**
 * Seeds a default Platform Admin and a Default Tenant + User for local development.
 */
@Slf4j
@Component
@Profile({"local", "prod", "default"})
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final PlatformAdminRepository platformAdminRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessTypeRepository businessTypeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) {
        log.info("Checking if Platform Admin needs to be seeded...");
        if (platformAdminRepository.count() == 0) {
            log.info("No Platform Admin found. Seeding default admin (admin@spiceflow.com / password)...");
            
            PlatformAdmin admin = PlatformAdmin.builder()
                .name("Super Admin")
                .email("admin@spiceflow.com")
                .passwordHash(java.util.Objects.requireNonNull(passwordEncoder.encode("password"), "Password hash cannot be null"))
                .build();
                
            admin.setCreatedAt(OffsetDateTime.now(ZoneId.systemDefault()));
            admin.setUpdatedAt(OffsetDateTime.now(ZoneId.systemDefault()));
            
            platformAdminRepository.save(admin);
            log.info("Successfully seeded default Platform Admin!");
        } else {
            log.info("Platform Admin already exists. Skipping seed.");
        }

        log.info("Checking if Tenant User needs to be seeded...");
        if (tenantRepository.count() == 0) {
            log.info("No Tenant found. Seeding default tenant and user (user@spiceflow.com / password)...");

            BusinessType businessType = businessTypeRepository.findByName("SPICE")
                .orElseGet(() -> {
                    BusinessType newBt = BusinessType.builder()
                        .name("SPICE")
                        .description("Spice manufacturing and distribution")
                        .build();
                    return businessTypeRepository.save(newBt);
                });

            Tenant tenant = Tenant.builder()
                .businessName("Default Spice Business")
                .email("business@spiceflow.com")
                .status("ACTIVE")
                .plan("PREMIUM")
                .businessType(businessType)
                .trialStartDate(LocalDate.now(ZoneId.systemDefault()))
                .build();
            tenant.setCreatedAt(OffsetDateTime.now(ZoneId.systemDefault()));
            tenant.setUpdatedAt(OffsetDateTime.now(ZoneId.systemDefault()));
            tenant = tenantRepository.save(tenant);

            Role ownerRole = Role.builder()
                .tenant(tenant)
                .name("TENANT OWNER")
                .description("Full access to everything in the tenant")
                .isSystemRole(true)
                .permissions(new HashSet<>(permissionRepository.findAll()))
                .build();
            ownerRole.setCreatedAt(OffsetDateTime.now(ZoneId.systemDefault()));
            ownerRole.setUpdatedAt(OffsetDateTime.now(ZoneId.systemDefault()));
            ownerRole = roleRepository.save(ownerRole);

            User user = User.builder()
                .tenant(tenant)
                .email("user@spiceflow.com")
                .passwordHash(java.util.Objects.requireNonNull(passwordEncoder.encode("password"), "Password hash cannot be null"))
                .assignedRole(ownerRole)
                .passwordChangeRequired(false)
                .failedLoginAttempts(0)
                .build();
            user.setCreatedAt(OffsetDateTime.now(ZoneId.systemDefault()));
            user.setUpdatedAt(OffsetDateTime.now(ZoneId.systemDefault()));
            userRepository.save(user);

            log.info("Successfully seeded default Tenant and User!");
        } else {
            log.info("Tenant already exists. Skipping seed.");
        }
    }
}



