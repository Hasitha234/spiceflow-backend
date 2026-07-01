package com.spiceflow.backend.auth.config;

import com.spiceflow.backend.auth.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import com.spiceflow.backend.security.config.RateLimitFilter;


/** Spring Security configuration — stateless JWT, no sessions, no CSRF. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final HandlerExceptionResolver exceptionResolver;
  private final RateLimitFilter rateLimitFilter;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
      RateLimitFilter rateLimitFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.exceptionResolver = exceptionResolver;
    this.rateLimitFilter = rateLimitFilter;
  }


  /** Defines which endpoints are public and which require authentication. */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(Customizer.withDefaults()) // Enable CORS
        .csrf(AbstractHttpConfigurer::disable) // Stateless JWT — CSRF not needed
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            .frameOptions(frame -> frame.deny())
            .xssProtection(xss -> xss.disable()) // Rely on CSP
            .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Public endpoints — no JWT required
            .requestMatchers(HttpMethod.POST,
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/forgot-password",
                "/api/v1/auth/reset-password"
            ).permitAll()
            .requestMatchers(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**"
            ).permitAll()
            .requestMatchers("/actuator/**").hasRole("ADMIN")
            // Everything else requires a valid JWT
            .anyRequest().authenticated()
        )
        // Force Security exceptions to the GlobalExceptionHandler
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, authException) -> 
                exceptionResolver.resolveException(request, response, null, authException))
            .accessDeniedHandler((request, response, accessDeniedException) -> 
                exceptionResolver.resolveException(request, response, null, accessDeniedException))
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);
        
    return http.build();
  }


  /** BCrypt password encoder with cost factor 12 — industry standard for password hashing. */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
