package com.document.example.commons.security;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@ToString
public class UserPrincipal implements Authentication {

    private final UUID userId;

    private final Set<SimpleGrantedAuthority> authorities;

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return this;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}
