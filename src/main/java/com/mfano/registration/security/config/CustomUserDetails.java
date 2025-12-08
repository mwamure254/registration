package com.mfano.registration.security.config;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mfano.registration.security.model.User;

public class CustomUserDetails implements UserDetails {

    private User user;

    public User getUser() {
        return user;
    }
    
    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(r -> (GrantedAuthority) () -> "ROLE_" + r.getName())
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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
        return user.isEnabled();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getFin() {
        return user.getFin();
    }

    public String getLan() {
        return user.getLan();
    }

    public Long getId() { return user.getId(); }
    public Set<String> getRoles() { return user.getRoles().stream().map(r->r.getName()).collect(Collectors.toSet()); }
    
}
