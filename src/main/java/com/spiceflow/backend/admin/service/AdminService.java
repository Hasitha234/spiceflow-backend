package com.spiceflow.backend.admin.service;

import com.spiceflow.backend.admin.dto.request.CreateTenantRequest;
import com.spiceflow.backend.admin.dto.response.TenantResponse;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.auth.entity.User;
import com.spiceflow.backend.admin.dto.request.UpdateTenantRequest;
import com.spiceflow.backend.common.exception.ResourceConflictException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.spiceflow.backend.common.dto.PageResponse;
import com.spiceflow.backend.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.spiceflow.backend.admin.dto.request.UpdateTenantRequest;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.spiceflow.backend.auth.entity.Permission;
import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.repository.PermissionRepository;
import com.spiceflow.backend.auth.repository.RoleRepository;
import java.util.HashSet;


@Service
@Transactional(readOnly = true)
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public AdminService(TenantRepository tenantRepository, UserRepository userRepository,
        PasswordEncoder passwordEncoder, PermissionRepository permissionRepository,
        RoleRepository roleRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }


    /**
     * Atomically creates a new Tenant (business) and its Owner User account.
     */
        @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        
        // 1. Validate that the email is not already used by another tenant
        if (tenantRepository.findByEmailAndDeletedAtIsNull(request.getOwnerEmail()).isPresent()) {
            throw new ResourceConflictException("Email is already registered to a business");
        }

        // 2. Create the Tenant
        Tenant tenant = Tenant.builder()
            .businessName(request.getBusinessName())
            .businessType(request.getBusinessType())
            .email(request.getOwnerEmail())
            .status("ACTIVE")
            .plan("BASIC")
            .build();
            
        tenant = tenantRepository.save(tenant);

        // 3. Create the default "Owner" role with ALL permissions
        Role ownerRole = Role.builder()
            .tenant(tenant)
            .name("Owner")
            .description("Full access to all modules — auto-created by system")
            .isSystemRole(true)
            .permissions(new HashSet<>(permissionRepository.findAll()))
            .build();
        
        ownerRole = roleRepository.save(ownerRole);

        // 4. Create the Tenant Owner User with the Owner role assigned
        User owner = User.builder()
            .tenant(tenant)
            .email(request.getOwnerEmail())
            .passwordHash(passwordEncoder.encode(request.getOwnerPassword()))
            .assignedRole(ownerRole)
            .build();
            
        userRepository.save(owner);

        log.info("Platform Admin created new {} business: {} with owner {}", 
            request.getBusinessType(), request.getBusinessName(), request.getOwnerEmail());

        // 5. Map the newly created tenant back to a safe Response DTO
        return TenantResponse.builder()
            .id(tenant.getId())
            .businessName(tenant.getBusinessName())
            .businessType(tenant.getBusinessType())
            .email(tenant.getEmail())
            .status(tenant.getStatus())
            .plan(tenant.getPlan())
            .createdAt(tenant.getCreatedAt())
            .build();
    }


  /** Returns a list of all active (non-deleted) tenants with pagination. */
    public PageResponse<TenantResponse> getAllTenants(Pageable pageable) {
    Page<Tenant> tenants = tenantRepository.findAllByDeletedAtIsNull(pageable);
    
    log.debug("Fetched {} active tenants from database", tenants.getNumberOfElements());
    
    Page<TenantResponse> responsePage = tenants.map(t -> TenantResponse.builder()
            .id(t.getId())
            .businessName(t.getBusinessName())
            .businessType(t.getBusinessType())
            .email(t.getEmail())
            .status(t.getStatus())
            .plan(t.getPlan())
            .createdAt(t.getCreatedAt())
            .build());
            
    return PageResponse.of(responsePage);
  }


  /** Returns a single tenant by ID, or throws 404 if not found or deleted. */
  public TenantResponse getTenantById(Long id) {
    Tenant tenant = tenantRepository.findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

    log.debug("Fetched tenant id={} from database", tenant.getId());
    
    return TenantResponse.builder()
        .id(tenant.getId())
        .businessName(tenant.getBusinessName())
        .businessType(tenant.getBusinessType())
        .email(tenant.getEmail())
        .status(tenant.getStatus())
        .plan(tenant.getPlan())
        .createdAt(tenant.getCreatedAt())
        .build();
  }

  /** Updates a tenant's business details. Email cannot be changed. */
  @Transactional
  public TenantResponse updateTenant(Long id, UpdateTenantRequest request) {
    Tenant tenant = tenantRepository.findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

    tenant.setBusinessName(request.getBusinessName());
    tenant.setBusinessType(request.getBusinessType());
    tenant.setStatus(request.getStatus());
    tenant.setPlan(request.getPlan());
    tenantRepository.save(tenant);

    log.info("Platform Admin updated tenant id={}, name={}", tenant.getId(), tenant.getBusinessName());

    return TenantResponse.builder()
        .id(tenant.getId())
        .businessName(tenant.getBusinessName())
        .businessType(tenant.getBusinessType())
        .email(tenant.getEmail())
        .status(tenant.getStatus())
        .plan(tenant.getPlan())
        .createdAt(tenant.getCreatedAt())
        .build();
  }

  /** Soft-deletes a tenant — sets deleted_at timestamp, never removes the database record. */
  @Transactional
  public void deleteTenant(Long id) {
    Tenant tenant = tenantRepository.findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

    tenant.setDeletedAt(OffsetDateTime.now());
    tenantRepository.save(tenant);
    log.info("Platform Admin soft-deleted tenant id={}, name={}", tenant.getId(), tenant.getBusinessName());
  }
}
