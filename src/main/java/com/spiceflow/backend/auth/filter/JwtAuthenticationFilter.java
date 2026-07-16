package com.spiceflow.backend.auth.filter;

import com.spiceflow.backend.admin.repository.PlatformAdminRepository;
import com.spiceflow.backend.auth.repository.UserRepository;
import com.spiceflow.backend.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** Reads the JWT from the Authorization header and sets the SecurityContext. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final PlatformAdminRepository platformAdminRepository;
  private final com.spiceflow.backend.auth.service.TokenBlacklistService tokenBlacklistService;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository,
      PlatformAdminRepository platformAdminRepository,
      com.spiceflow.backend.auth.service.TokenBlacklistService tokenBlacklistService) {
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
    this.platformAdminRepository = platformAdminRepository;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String token = extractToken(request);

    if (token != null && !tokenBlacklistService.isBlacklisted(token) && jwtUtil.isTokenValid(token)) {
      String email = jwtUtil.extractEmail(token);
      String userType = jwtUtil.extractUserType(token);

      if (email != null && userType != null
          && SecurityContextHolder.getContext().getAuthentication() == null) {

        if ("PLATFORM_ADMIN".equals(userType)) {
          platformAdminRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(admin -> {
            if (admin.isEnabled()) {
              com.spiceflow.backend.auth.dto.AuthenticatedUser authUser = com.spiceflow.backend.auth.dto.AuthenticatedUser.builder()
                  .id(admin.getId())
                  .email(admin.getEmail())
                  .authorities(admin.getAuthorities())
                  .accountNonExpired(true)
                  .accountNonLocked(true)
                  .credentialsNonExpired(true)
                  .enabled(admin.isEnabled())
                  .build();
                  
              UsernamePasswordAuthenticationToken auth =
                  new UsernamePasswordAuthenticationToken(
                      authUser, null, authUser.getAuthorities());
              auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(auth);
              org.slf4j.MDC.put("userId", admin.getEmail());
              log.debug("Authenticated platform admin: {}", email);
            }
          });
        } else {
          boolean[] shouldBlock = new boolean[]{false};
          userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(user -> {
            if (user.isEnabled() && user.isAccountNonLocked()) {
              Long activeTenantId = user.getTenantId();
              
              if ("TENANT_OWNER".equals(userType)) {
                  String tenantHeader = request.getHeader("X-Tenant-ID");
                  if (org.springframework.util.StringUtils.hasText(tenantHeader)) {
                      try {
                          Long requestedTenantId = Long.parseLong(tenantHeader);
                          
                          // verify they own it from claims (or database if claims aren't sufficient)
                          Object assignedClaim = jwtUtil.parseClaims(token).get("assignedTenants");
                          if (assignedClaim instanceof java.util.List) {
                              java.util.List<?> assigned = (java.util.List<?>) assignedClaim;
                              boolean hasTenant = assigned.stream().anyMatch(item -> {
                                  if (item instanceof java.util.Map) {
                                      Object idObj = ((java.util.Map<?, ?>) item).get("id");
                                      if (idObj instanceof Number) {
                                          return ((Number) idObj).longValue() == requestedTenantId.longValue();
                                      }
                                  }
                                  return false;
                              });
                              if (hasTenant) {
                                  activeTenantId = requestedTenantId;
                              }
                          }
                      } catch (NumberFormatException e) {
                          // ignore bad header
                      }
                  } else {
                      activeTenantId = null; // Forces them to be "tenant-less" until they pick one
                  }
              }
              
              // Extract status from JWT claims to check if the tenant is active
              String activeTenantStatus = null;
              Object assignedClaim = jwtUtil.parseClaims(token).get("assignedTenants");
              if (assignedClaim instanceof java.util.List) {
                  java.util.List<?> assigned = (java.util.List<?>) assignedClaim;
                  for (Object item : assigned) {
                      if (item instanceof java.util.Map) {
                          java.util.Map<?, ?> tenantMap = (java.util.Map<?, ?>) item;
                          Object idObj = tenantMap.get("id");
                          if (idObj instanceof Number && activeTenantId != null && ((Number) idObj).longValue() == activeTenantId.longValue()) {
                              activeTenantStatus = (String) tenantMap.get("status");
                              break;
                          }
                      }
                  }
              }
              
              if ("EXPIRED".equals(activeTenantStatus) || "DISABLED".equals(activeTenantStatus)) {
                  shouldBlock[0] = true;
                  return;
              }

              com.spiceflow.backend.auth.dto.AuthenticatedUser authUser = com.spiceflow.backend.auth.dto.AuthenticatedUser.builder()
                  .id(user.getId())
                  .email(user.getEmail())
                  .tenantId(activeTenantId)
                  .authorities(user.getAuthorities())
                  .accountNonExpired(true)
                  .accountNonLocked(user.isAccountNonLocked())
                  .credentialsNonExpired(true)
                  .enabled(user.isEnabled())
                  .build();

              UsernamePasswordAuthenticationToken auth =
                  new UsernamePasswordAuthenticationToken(
                      authUser, null, authUser.getAuthorities());
              auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(auth);
              org.slf4j.MDC.put("userId", user.getEmail());
              if (activeTenantId != null) {
                  com.spiceflow.backend.common.context.TenantContext.setTenantId(activeTenantId);
                  org.slf4j.MDC.put("tenantId", activeTenantId.toString());
              }
              log.debug("Authenticated tenant user: {} for active tenant: {}",
                  email, activeTenantId);
            }
          });
          
          if (shouldBlock[0]) {
              response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant is expired or disabled");
              return;
          }
        }
      }
    }
    
    try {
      chain.doFilter(request, response);
    } finally {
      org.slf4j.MDC.remove("userId");
      org.slf4j.MDC.remove("tenantId");
      com.spiceflow.backend.common.context.TenantContext.clear();
    }
  }

  private @org.jspecify.annotations.Nullable String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}

