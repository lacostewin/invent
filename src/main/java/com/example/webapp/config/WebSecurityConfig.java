package com.example.webapp.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;


// Доменная авторизация контроллером Spring
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/home", "/logout/**", "/logout-success", "/login/**").permitAll()
                .antMatchers("/**").hasRole("INVENT_ADMIN")
                .antMatchers("/search/**", "/static/css.**").hasRole("INVENT_USER")
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
                .userSearchFilter("(&(objectClass=person)(objectClass=user)(sAMAccountName={0})(|(memberOf=cn=invent_admin,OU=inventorization,OU=Groups,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru)(memberOf=cn=invent_user,OU=inventorization,OU=Groups,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru)))")
                .userSearchBase("OU=Active,OU=Users,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .groupSearchBase("OU=inventorization,OU=Groups,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .groupSearchFilter("(member={0})")

                .contextSource()
                .url("ldap://regions.office.np-ivc.ru:389")
                .managerDn("CN=ldap_user_ro,OU=Service,OU=Users,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .managerPassword("i8wx6NzLssM4");
    }
}