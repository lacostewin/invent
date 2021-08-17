package com.example.webapp.config;
import com.example.webapp.filters.CsrfLoggerFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;

// Доменная авторизация контроллером Spring и назначение прав достпа к страницам
@Configuration
@EnableAutoConfiguration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)

class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception  {
        http
                .authorizeRequests()
                .antMatchers("/logout/**", "/logout-success", "/login/**", "/static/**", "/**.png").permitAll()
                .antMatchers("/main").hasRole("INVENTADMIN")
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
                .and()
                .logout()
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .permitAll()
                .and()
                .exceptionHandling().accessDeniedPage("/403")
        .and()
        .httpBasic();
        http.addFilterAfter(new CsrfLoggerFilter(), CsrfFilter.class)
        ;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authBuilder) throws Exception {
        authBuilder
                .ldapAuthentication()
                .userSearchFilter("(&(objectClass=person)(objectClass=user)(sAMAccountName={0})(|(memberOf=CN=inventadmin,OU=inventorization,OU=Groups,OU=nsk,OU=All,DC=regions,DC=office,DC=np-ivc,DC=ru)(memberOf=CN=inventuser,OU=inventorization,OU=Groups,OU=nsk,OU=All,DC=regions,DC=office,DC=np-ivc,DC=ru)))")
                .userSearchBase("OU=All,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .groupSearchBase("OU=inventorization,OU=Groups,OU=nsk,OU=All,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .groupSearchFilter("(member={0})")
                .contextSource()
                .url("ldap://regions.office.np-ivc.ru:389")
                .managerDn("CN=ldap_user_ro,OU=Service,OU=Users,OU=nsk,OU=All,DC=regions,DC=office,DC=np-ivc,DC=ru")
                .managerPassword("i8wx6NzLssM4");
    }
}