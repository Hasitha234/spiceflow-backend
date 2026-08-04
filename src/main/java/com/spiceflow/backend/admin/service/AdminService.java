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
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
    private final com.spiceflow.backend.auth.repository.BusinessOwnerTenantRepository businessOwnerTenantRepository;

    public AdminService(TenantRepository tenantRepository, UserRepository userRepository,
        PasswordEncoder passwordEncoder, PermissionRepository permissionRepository,
        RoleRepository roleRepository, BusinessTypeRepository businessTypeRepository,
        WarehouseRepository warehouseRepository,
        com.spiceflow.backend.auth.repository.BusinessOwnerTenantRepository businessOwnerTenantRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.businessTypeRepository = businessTypeRepository;
        this.warehouseRepository = warehouseRepository;
        this.businessOwnerTenantRepository = businessOwnerTenantRepository;
    }


    /**
     * Atomically creates a new Tenant (business) and its Owner User account.
     */
        @Transactional(rollbackFor = Exception.class)
    public TenantResponse createTenant(CreateTenantRequest request) {
        
        // 1. Validate that the email is not already used by another tenant or user
        if (tenantRepository.findByEmailAndDeletedAtIsNull(request.ownerEmail()).isPresent()) {
            throw new ResourceConflictException("Email is already registered to a business");
        }
        if (userRepository.findByEmailAndDeletedAtIsNull(request.ownerEmail()).isPresent()) {
            throw new ResourceConflictException("A user account with this email already exists");
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

        // 4. Create the Tenant Owner User with the multi-agency multi-tenant structure
        User owner = User.builder()
            // Do NOT link directly via tenant_id
            .name(request.businessName() + " Owner")
            .email(request.ownerEmail())
            .passwordHash(java.util.Objects.requireNonNull(passwordEncoder.encode(request.ownerPassword()), "Password hash cannot be null"))
            .userType("TENANT_OWNER")
            .assignedRole(ownerRole)
            .build();
        owner = userRepository.save(owner);
        
        // Map the new user to the tenant via the join table
        com.spiceflow.backend.auth.entity.BusinessOwnerTenant bot = com.spiceflow.backend.auth.entity.BusinessOwnerTenant.builder()
            .user(owner)
            .tenant(tenant)
            .build();
        businessOwnerTenantRepository.save(bot);

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
  @Transactional(readOnly = true)
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
  @Transactional(readOnly = true)
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

  @Transactional(rollbackFor = Exception.class)
  public TenantResponse updateTenantStatus(Long id, com.spiceflow.backend.admin.dto.request.UpdateTenantStatusRequest request) {
    Tenant tenant = tenantRepository.findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

    tenant.setStatus(request.status());
    tenantRepository.save(tenant);

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

  // --- USER MANAGEMENT ---

  @Transactional(rollbackFor = Exception.class)
  public com.spiceflow.backend.admin.dto.response.UserResponse createUser(com.spiceflow.backend.admin.dto.request.CreateUserRequest request) {
      if (userRepository.existsByEmailIncludingDeleted(request.email())) {
          throw new ResourceConflictException("Email is already registered");
      }

      User user = User.builder()
          .name(request.name())
          .email(request.email())
          .passwordHash(java.util.Objects.requireNonNull(passwordEncoder.encode(request.password()), "Password hash cannot be null"))
          .userType(request.userType())
          .build();

      if ("TENANT_OWNER".equals(request.userType())) {
          // No single tenantId, will have join records
          userRepository.save(user);
          if (request.tenantIds() != null) {
              for (Long tId : request.tenantIds()) {
                  assignTenantToOwner(user.getId(), tId);
              }
          }
      } else {
          // DATA_ENTRY or DRIVER need a specific tenant
          if (request.tenantId() == null) {
              throw new IllegalArgumentException("Tenant ID is required for user type: " + request.userType());
          }
          Tenant tenant = tenantRepository.findById(request.tenantId())
              .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
          
          user.setTenant(tenant);
          user.setAssignedRole(getOrCreateSystemRole(tenant, request.userType()));
          userRepository.save(user);
      }

      return mapToUserResponse(user);
  }

  @Transactional(readOnly = true)
  public PageResponse<com.spiceflow.backend.admin.dto.response.UserResponse> getAllUsers(Pageable pageable) {
      Page<User> users = userRepository.findAllByDeletedAtIsNull(pageable);
      return PageResponse.of(users.map(this::mapToUserResponse));
  }

  @Transactional(readOnly = true)
  public com.spiceflow.backend.admin.dto.response.UserResponse getUserById(Long id) {
      User user = userRepository.findById(id)
          .filter(u -> u.getDeletedAt() == null)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      return mapToUserResponse(user);
  }

  @Transactional(rollbackFor = Exception.class)
  public com.spiceflow.backend.admin.dto.response.UserResponse updateUser(Long id, com.spiceflow.backend.admin.dto.request.UpdateUserRequest request) {
      User user = userRepository.findById(id)
          .filter(u -> u.getDeletedAt() == null)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      
      // Update basic details if present...
      if (request.email() != null && !request.email().equals(user.getEmail())) {
          if (userRepository.existsByEmailIncludingDeleted(request.email())) {
              throw new ResourceConflictException("Email is already registered");
          }
          user.setEmail(request.email());
      }
      if (request.name() != null) {
          user.setName(request.name());
      }
      
      // E.g., re-assigning tenants, changing userType. Simplified for now.
      
      if ("TENANT_OWNER".equals(request.userType()) && request.tenantIds() != null) {
          // simple replacement strategy: delete all, add new
          List<com.spiceflow.backend.auth.entity.BusinessOwnerTenant> existing = businessOwnerTenantRepository.findByUserId(id);
          businessOwnerTenantRepository.deleteAll(existing);
          
          for (Long tId : request.tenantIds()) {
              assignTenantToOwner(id, tId);
          }
      }
      
      return mapToUserResponse(userRepository.save(user));
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteUser(Long id) {
      User user = userRepository.findById(id)
          .filter(u -> u.getDeletedAt() == null)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      user.setDeletedAt(OffsetDateTime.now(ZoneId.systemDefault()));
      userRepository.save(user);
  }

  @Transactional(rollbackFor = Exception.class)
  public void assignTenantToOwner(Long userId, Long tenantId) {
      User user = userRepository.findById(userId).orElseThrow();
      Tenant tenant = tenantRepository.findById(tenantId).orElseThrow();
      com.spiceflow.backend.auth.entity.BusinessOwnerTenant bot = com.spiceflow.backend.auth.entity.BusinessOwnerTenant.builder()
          .user(user)
          .tenant(tenant)
          .build();
      businessOwnerTenantRepository.save(bot);
  }

  @Transactional(rollbackFor = Exception.class)
  public void removeTenantFromOwner(Long userId, Long tenantId) {
      businessOwnerTenantRepository.deleteByUserIdAndTenantId(userId, tenantId);
  }

  private Role getOrCreateSystemRole(Tenant tenant, String userType) {
      String roleName = "DATA_ENTRY".equals(userType) ? "Data Entry" : "Driver";
      java.util.Set<String> permCodes = "DATA_ENTRY".equals(userType)
          ? java.util.Set.of("INVENTORY_VIEW", "INVENTORY_TRANSFER", "PURCHASE_VIEW", "PURCHASE_CREATE", "PURCHASE_UPDATE", "ORDER_VIEW", "ORDER_CREATE", "ORDER_UPDATE", "LOADING_VIEW", "LOADING_CREATE", "LOADING_CONFIRM", "DELIVERY_VIEW", "DELIVERY_CREATE", "DELIVERY_UPDATE", "SETTINGS_PRODUCTS", "SETTINGS_SHOPS", "SETTINGS_REPS", "SETTINGS_DRIVERS", "SETTINGS_SUPPLIERS", "STORE_VIEW")
          : java.util.Set.of("LOADING_VIEW", "DELIVERY_VIEW", "DELIVERY_CREATE", "DELIVERY_UPDATE");

      return roleRepository.findByTenantId(tenant.getId()).stream()
          .filter(r -> roleName.equals(r.getName()))
          .findFirst()
          .map(existingRole -> {
              List<com.spiceflow.backend.auth.entity.Permission> allPerms = permissionRepository.findAll().stream()
                  .filter(p -> permCodes.contains(p.getCode()))
                  .collect(Collectors.toList());
              existingRole.setPermissions(new HashSet<>(allPerms));
              return roleRepository.save(existingRole);
          })
          .orElseGet(() -> {
              List<com.spiceflow.backend.auth.entity.Permission> perms = permissionRepository.findAll().stream()
                  .filter(p -> permCodes.contains(p.getCode()))
                  .collect(Collectors.toList());
              
              Role role = Role.builder()
                  .tenant(tenant)
                  .name(roleName)
                  .description("Auto-created system role for " + roleName)
                  .isSystemRole(true)
                  .permissions(new HashSet<>(perms))
                  .build();
              return roleRepository.save(role);
          });
  }

  private com.spiceflow.backend.admin.dto.response.UserResponse mapToUserResponse(User user) {
      List<com.spiceflow.backend.admin.dto.response.TenantAssignedResponse> assigned = List.of();
      if ("TENANT_OWNER".equals(user.getUserType())) {
          assigned = businessOwnerTenantRepository.findByUserId(user.getId()).stream()
              .map(bt -> new com.spiceflow.backend.admin.dto.response.TenantAssignedResponse(
                  bt.getTenant().getId(), bt.getTenant().getBusinessName(), bt.getTenant().getStatus()))
              .collect(Collectors.toList());
      }
      
      return new com.spiceflow.backend.admin.dto.response.UserResponse(
          user.getId(),
          user.getName(),
          user.getEmail(),
          user.getUserType(),
          user.getTenantId() != null ? user.getTenantId() : -1L,
          user.getTenant() != null ? user.getTenant().getBusinessName() : "",
          user.getAssignedRole() != null ? user.getAssignedRole().getName() : "",
          assigned,
          user.getCreatedAt()
      );
  }
}
