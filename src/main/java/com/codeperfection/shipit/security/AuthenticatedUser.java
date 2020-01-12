package com.codeperfection.shipit.security;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

@Builder
@EqualsAndHashCode(of = "uuid")
public class AuthenticatedUser implements UserDetails {

    private UUID uuid;

    private String username;

    private String password;

    private OffsetDateTime passwordChangeDate;

    private Collection<? extends GrantedAuthority> authorities;

    public UUID getUuid() {
        return uuid;
    }

    public OffsetDateTime getPasswordChangeDate() {
        return passwordChangeDate;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
