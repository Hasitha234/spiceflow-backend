package com.spiceflow.backend.admin.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAdminTest {

    @Test
    @DisplayName("PlatformAdmin UserDetails contract and builder properties")
    void testPlatformAdmin() {
        PlatformAdmin admin = PlatformAdmin.builder()
                .name("Super Admin")
                .email("admin@spiceflow.com")
                .passwordHash("hashed")
                .build();

        assertThat(admin.getName()).isEqualTo("Super Admin");
        assertThat(admin.getEmail()).isEqualTo("admin@spiceflow.com");
        assertThat(admin.getPassword()).isEqualTo("hashed");
        assertThat(admin.getUsername()).isEqualTo("admin@spiceflow.com");

        Collection<? extends GrantedAuthority> authorities = admin.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_SUPER_ADMIN");

        assertThat(admin.isAccountNonExpired()).isTrue();
        assertThat(admin.isAccountNonLocked()).isTrue();
        assertThat(admin.isCredentialsNonExpired()).isTrue();
        assertThat(admin.isEnabled()).isTrue();

        admin.setEmail("new@spiceflow.com");
        admin.setName("New Admin");
        admin.setPasswordHash("newHash");

        assertThat(admin.getEmail()).isEqualTo("new@spiceflow.com");
        assertThat(admin.getName()).isEqualTo("New Admin");
        assertThat(admin.getPassword()).isEqualTo("newHash");
    }
}
