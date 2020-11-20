package com.example.webapp.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

// Доменная авторизация контроллером Spring и назначение прав достпа в страницам
@Configuration
@EnableAutoConfiguration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)

class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/logout/**", "/logout-success", "/login/**", "/static/**").permitAll()
                .antMatchers("/main").hasRole("INVENTADMIN")
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .and()
                .logout().permitAll()
                .and()
                .exceptionHandling().accessDeniedPage("/403");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authBuilder) throws Exception {

        authBuilder
                .ldapAuthentication()
                .userSearchFilter("(&(objectClass=person)(objectClass=user)(sAMAccountName={0})(|(memberOf=cn=inventadmin,OU=inventorization,OU=Groups,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru)(memberOf=cn=inventuser,OU=inventorization,OU=Groups,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru)))")
                .userSearchBase("OU=Active,OU=Users,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .groupSearchBase("OU=inventorization,OU=Groups,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .groupSearchFilter("(member={0})")
                .contextSource()
                .url("ldap://regions.office.np-ivc.ru:389")
                .managerDn("CN=ldap_user_ro,OU=Service,OU=Users,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .managerPassword("i8wx6NzLssM4");
    }
}