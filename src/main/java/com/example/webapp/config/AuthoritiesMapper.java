package com.example.webapp.config;
import com.example.webapp.repos.Roles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class AuthoritiesMapper implements GrantedAuthoritiesMapper {
    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<Roles> roles = EnumSet.noneOf(Roles.class);

        for (GrantedAuthority a: authorities) {
            if ("invent_admin".equals(a.getAuthority())) {
                roles.add(Roles.INVENT_ADMIN);
            } else if ("invent_user".equals(a.getAuthority())) {
                roles.add(Roles.INVENT_USER);
            }
        }
        return roles;
    }
}