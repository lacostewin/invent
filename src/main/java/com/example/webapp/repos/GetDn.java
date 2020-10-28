package com.example.webapp.repos;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

public interface GetDn {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((LdapUserDetails) principal).getDn();
    String displayName = username.split("\\,")[0].split("=")[1];
}