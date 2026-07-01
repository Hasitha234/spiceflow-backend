package com.spiceflow.backend.common.config;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() 
            || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of("SYSTEM"); // Fallback for auto-created records
        }

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return Optional.of(userPrincipal.getUsername()); // Returns the email
    }
}
