package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests(authz -> authz
                .antMatchers("/test/idam/users/**").hasAuthority("SCOPE_profile")
                .antMatchers("/test/idam/notifications/**").hasAuthority("SCOPE_profile")
                .antMatchers("/test/idam/burner/users/**").permitAll()
                .antMatchers("/test/prd/users/**").hasAuthority("SCOPE_profile")
                .anyRequest().permitAll())
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
    }

}
