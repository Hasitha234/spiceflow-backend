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
              UsernamePasswordAuthenticationToken auth =
                  new UsernamePasswordAuthenticationToken(
                      admin, null, admin.getAuthorities());
              auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(auth);
              org.slf4j.MDC.put("userId", admin.getEmail());
              log.debug("Authenticated platform admin: {}", email);
            }
          });
        } else {
          userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(user -> {
            if (user.isEnabled() && user.isAccountNonLocked()) {
              UsernamePasswordAuthenticationToken auth =
                  new UsernamePasswordAuthenticationToken(
                      user, null, user.getAuthorities());
              auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(auth);
              org.slf4j.MDC.put("userId", user.getEmail());
              log.debug("Authenticated tenant user: {} for tenant: {}",
                  email, user.getTenantId());
            }
          });
        }
      }
    }
    
    try {
      chain.doFilter(request, response);
    } finally {
      org.slf4j.MDC.remove("userId");
    }
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
