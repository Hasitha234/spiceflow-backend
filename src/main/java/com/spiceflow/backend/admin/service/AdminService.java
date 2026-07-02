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
        if (tenantRepository.findByEmailAndDeletedAtIsNull(request.getOwnerEmail()).isPresent()) {
            throw new ResourceConflictException("Email is already registered to a business");
        }

        BusinessType businessType = businessTypeRepository.findById(request.getBusinessTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("Business Type not found with id: " + request.getBusinessTypeId()));

        // 2. Create the Tenant
        Tenant tenant = Tenant.builder()
            .businessName(request.getBusinessName())
            .businessType(businessType)
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

        // 4.5. Seed default System Stores
        List<Warehouse> defaultStores = List.of(
            Warehouse.builder().tenant(tenant).name("Main Store").storeType("MAIN").isSystemStore(true).description("Default primary store").build(),
            Warehouse.builder().tenant(tenant).name("Second Store").storeType("SECONDARY").isSystemStore(true).description("Default secondary store for excess").build(),
            Warehouse.builder().tenant(tenant).name("Closed-Shop Returns").storeType("CLOSED_SHOP_RETURNS").isSystemStore(true).description("Store for items returned from closed shops").build(),
            Warehouse.builder().tenant(tenant).name("Expired Returns").storeType("EXPIRED_RETURNS").isSystemStore(true).description("Store for expired/damaged items").build()
        );
        warehouseRepository.saveAll(defaultStores);

        log.info("Platform Admin created new {} business: {} with owner {}", 
            businessType.getName(), request.getBusinessName(), request.getOwnerEmail());

        // 5. Map the newly created tenant back to a safe Response DTO
        return TenantResponse.builder()
            .id(tenant.getId())
            .businessName(tenant.getBusinessName())
            .businessTypeId(tenant.getBusinessType().getId())
            .businessTypeName(tenant.getBusinessType().getName())
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
            .businessTypeId(t.getBusinessType().getId())
            .businessTypeName(t.getBusinessType().getName())
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
        .businessTypeId(tenant.getBusinessType().getId())
        .businessTypeName(tenant.getBusinessType().getName())
        .email(tenant.getEmail())
        .status(tenant.getStatus())
        .plan(tenant.getPlan())
        .createdAt(tenant.getCreatedAt())
        .build();
  }

  /** Updates a tenant's business details. Email cannot be changed. */
  @Transactional(rollbackFor = Exception.class)
  public TenantResponse updateTenant(Long id, UpdateTenantRequest request) {
    Tenant tenant = tenantRepository.findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

    BusinessType businessType = businessTypeRepository.findById(request.getBusinessTypeId())
        .orElseThrow(() -> new ResourceNotFoundException("Business Type not found with id: " + request.getBusinessTypeId()));

    tenant.setBusinessName(request.getBusinessName());
    tenant.setBusinessType(businessType);
    tenant.setStatus(request.getStatus());
    tenant.setPlan(request.getPlan());
    tenantRepository.save(tenant);

    log.info("Platform Admin updated tenant id={}, name={}", tenant.getId(), tenant.getBusinessName());

    return TenantResponse.builder()
        .id(tenant.getId())
        .businessName(tenant.getBusinessName())
        .businessTypeId(tenant.getBusinessType().getId())
        .businessTypeName(tenant.getBusinessType().getName())
        .email(tenant.getEmail())
        .status(tenant.getStatus())
        .plan(tenant.getPlan())
        .createdAt(tenant.getCreatedAt())
        .build();
  }

  /** Soft-deletes a tenant — sets deleted_at timestamp, never removes the database record. */
  @Transactional(rollbackFor = Exception.class)
  public void deleteTenant(Long id) {
    Tenant tenant = tenantRepository.findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

    tenant.setDeletedAt(OffsetDateTime.now());
    tenantRepository.save(tenant);
    log.info("Platform Admin soft-deleted tenant id={}, name={}", tenant.getId(), tenant.getBusinessName());
  }
}
