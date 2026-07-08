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
import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
import org.springframework.transaction.annotation.Transactional;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.spiceflow.backend.auth.entity.Role;
import com.spiceflow.backend.auth.repository.PermissionRepository;
import com.spiceflow.backend.auth.repository.RoleRepository;
import com.spiceflow.backend.inventory.entity.Warehouse;
import com.spiceflow.backend.inventory.repository.WarehouseRepository;
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
    private final BusinessTypeRepository businessTypeRepository;
    private final WarehouseRepository warehouseRepository;

    public AdminService(TenantRepository tenantRepository, UserRepository userRepository,
        PasswordEncoder passwordEncoder, PermissionRepository permissionRepository,
        RoleRepository roleRepository, BusinessTypeRepository businessTypeRepository,
        WarehouseRepository warehouseRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.businessTypeRepository = businessTypeRepository;
        this.warehouseRepository = warehouseRepository;
    }


    /**
     * Atomically creates a new Tenant (business) and its Owner User account.
     */
        @Transactional(rollbackFor = Exception.class)
    public TenantResponse createTenant(CreateTenantRequest request) {
        
        // 1. Validate that the email is not already used by another tenant
        if (tenantRepository.findByEmailAndDeletedAtIsNull(request.ownerEmail()).isPresent()) {
            throw new ResourceConflictException("Email is already registered to a business");
        }

        BusinessType businessType = businessTypeRepository.findById(request.businessTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("Business Type not found with id: " + request.businessTypeId()));

        // 2. Create the Tenant
        Tenant tenant = Tenant.builder()
            .businessName(request.businessName())
            .businessType(businessType)
            .email(request.ownerEmail())
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
            .email(request.ownerEmail())
            .passwordHash(java.util.Objects.requireNonNull(passwordEncoder.encode(request.ownerPassword()), "Password hash cannot be null"))
            .assignedRole(ownerRole)
            .build();
        userRepository.save(owner);

        // 4.5. Seed default System Stores
        List<Warehouse> defaultStores = List.of(
            Warehouse.builder().tenant(tenant).name("Main Store").storeType("MAIN").isSystemStore(true).description("Default primary store").build(),
            Warehouse.builder().tenant(tenant).name("Second Store").storeType("SECONDARY").isSystemStore(true).description("Default secondary store for excess").build(),
            Warehouse.builder().tenant(tenant).name("Closed-Shop Returns").storeType("CLOSED_SHOP_RETURNS").isSystemStore(true).description("Store for items returned from closed shops").build(),
            Warehouse.builder().tenant(tenant).name("Expired Returns").storeType("EXPIRED_RETURNS").isSystemStore(true).description("Store for expired/damaged items").build()
        );
        warehouseRepository.saveAll(defaultStores);

        log.info("Platform Admin created new {} business: {} with owner {}", 
            businessType.getName(), request.businessName(), request.ownerEmail());

        // 5. Map the newly created tenant back to a safe Response DTO
        return new TenantResponse(
            tenant.getId(),
            tenant.getBusinessName(),
            tenant.getBusinessType().getId(),
            tenant.getBusinessType().getName(),
            tenant.getEmail(),
            tenant.getStatus(),
            tenant.getPlan(),
            tenant.getCreatedAt()
        );
    }


  /** Returns a list of all active (non-deleted) tenants with pagination. */
    public PageResponse<TenantResponse> getAllTenants(Pageable pageable) {
    Page<Tenant> tenants = tenantRepository.findAllByDeletedAtIsNull(pageable);
    
    log.debug("Fetched {} active tenants from database", tenants.getNumberOfElements());
    
    Page<TenantResponse> responsePage = tenants.map(t -> new TenantResponse(
            t.getId(),
            t.getBusinessName(),
            t.getBusinessType().getId(),
            t.getBusinessType().getName(),
            t.getEmail(),
            t.getStatus(),
            t.getPlan(),
            t.getCreatedAt()
        ));
            
    return PageResponse.of(responsePage);
  }


  /** Returns a single tenant by ID, or throws 404 if not found or deleted. */
  public TenantResponse getTenantById(Long id) {
    Tenant tenant = tenantRepository.findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

    log.debug("Fetched tenant id={} from database", tenant.getId());
    
    return new TenantResponse(
        tenant.getId(),
        tenant.getBusinessName(),
        tenant.getBusinessType().getId(),
        tenant.getBusinessType().getName(),
        tenant.getEmail(),
        tenant.getStatus(),
        tenant.getPlan(),
        tenant.getCreatedAt()
    );
  }

  /** Updates a tenant's business details. Email cannot be changed. */
  @Transactional(rollbackFor = Exception.class)
  public TenantResponse updateTenant(Long id, UpdateTenantRequest request) {
    Tenant tenant = tenantRepository.findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

    BusinessType businessType = businessTypeRepository.findById(request.businessTypeId())
        .orElseThrow(() -> new ResourceNotFoundException("Business Type not found with id: " + request.businessTypeId()));

    tenant.setBusinessName(request.businessName());
    tenant.setBusinessType(businessType);
    tenant.setStatus(request.status());
    tenant.setPlan(request.plan());
    tenantRepository.save(tenant);

    log.info("Platform Admin updated tenant id={}, name={}", tenant.getId(), tenant.getBusinessName());

    return new TenantResponse(
        tenant.getId(),
        tenant.getBusinessName(),
        tenant.getBusinessType().getId(),
        tenant.getBusinessType().getName(),
        tenant.getEmail(),
        tenant.getStatus(),
        tenant.getPlan(),
        tenant.getCreatedAt()
    );
  }

  /** Soft-deletes a tenant — sets deleted_at timestamp, never removes the database record. */
  @Transactional(rollbackFor = Exception.class)
  public void deleteTenant(Long id) {
    Tenant tenant = tenantRepository.findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

    tenant.setDeletedAt(OffsetDateTime.now(java.time.ZoneId.systemDefault()));
    tenantRepository.save(tenant);
    log.info("Platform Admin soft-deleted tenant id={}, name={}", tenant.getId(), tenant.getBusinessName());
  }
}
