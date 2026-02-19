package com.epic.cms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain - ALL ENDPOINTS OPEN (No Authentication Required)");
        
        http
                .csrf(csrf -> {
                    csrf.disable();
                    log.info("CSRF protection disabled");
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    log.info("Session management set to STATELESS");
                })
                .authorizeHttpRequests(auth -> {
                    log.info("Authorization: Permitting ALL requests without authentication");
                    auth.anyRequest().permitAll();
                });

        log.info("Security Filter Chain configured successfully - No authentication required");
        return http.build();
    }
}
