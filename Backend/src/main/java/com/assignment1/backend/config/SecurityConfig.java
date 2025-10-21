package com.assignment1.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/health", "actuator/**", "/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                        // .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf
                                .disable()
                                // .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                // .ignoringRequestMatchers("/api/**")
                                // Keycloak
                )
                // .httpBasic(withDefaults());
                // no pop-up login
                .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }
}