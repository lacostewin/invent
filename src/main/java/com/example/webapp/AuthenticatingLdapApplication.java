package com.example.webapp;

import com.example.webapp.config.LdapSearch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.naming.NamingException;

@SpringBootApplication
public class AuthenticatingLdapApplication {

    public static void main(String[] args) throws NamingException {
        SpringApplication.run(AuthenticatingLdapApplication.class, args);
        LdapSearch app = new LdapSearch();
        app.ldapConnection();
        app.getAllUsers();
    }
}
