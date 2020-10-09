package com.example.webapp.config;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


// Доменная авторизация контроллером Spring
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/home", "/logout/**", "/logout-success", "/login/**").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .and()
                .logout()
                .permitAll();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authBuilder) throws Exception {
        authBuilder
                .ldapAuthentication()
                .userSearchFilter("(sAMAccountName={0})")
                .userSearchBase("OU=Active,OU=Users,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .groupSearchBase("OU=Groups,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .groupSearchFilter("memberOf={0}")
                .contextSource()
                .url("ldap://regions.office.np-ivc.ru:389")
                .managerDn("CN=ldap_user_ro,OU=Service,OU=Users,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .managerPassword("i8wx6NzLssM4");
    }

}