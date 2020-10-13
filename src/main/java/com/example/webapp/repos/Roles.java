package com.example.webapp.repos;
import org.springframework.security.core.GrantedAuthority;

public enum Roles implements GrantedAuthority {
    INVENT_ADMIN,
    INVENT_USER;

    public String getAuthority() {
        return name();
    }
}
