package com.spiceflow.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("NullAway.Init")
public class AuthenticatedUser implements UserDetails {

    private Long id;
    private String email;
    @org.jspecify.annotations.Nullable
    private Long tenantId;
    private Collection<? extends GrantedAuthority> authorities;
    @org.jspecify.annotations.Nullable
    private String password;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    
    @Override
    public String getUsername() {
        return email;
    }
}

