package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/test/idam/users/**").hasAuthority("SCOPE_profile")
                .requestMatchers("/test/idam/notifications/**").hasAuthority("SCOPE_profile")
                .requestMatchers("/admin/**").hasAuthority("SCOPE_profile")
                .requestMatchers("/test/idam/burner/users/**").permitAll()
                .anyRequest().permitAll())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

}
