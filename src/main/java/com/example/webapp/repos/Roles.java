package com.example.webapp.repos;
import org.springframework.security.core.GrantedAuthority;

public enum Roles implements GrantedAuthority {
    INVENTADMIN,
    INVENTUSER;

    public String getAuthority() {
        return name();
    }
}
