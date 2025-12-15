package com.x5.recruitment.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for RBAC.
 * Roles: ADMIN, RECRUITER, HM, CANDIDATE
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST API (no session cookies)
            // IMPORTANT: For production with web UI, enable CSRF protection for browser-based requests
            // This is acceptable for MVP as we use stateless authentication (HTTP Basic) without cookies
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/candidate/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Recruiter endpoints
                .requestMatchers("/api/recruiter/**").hasAnyRole("RECRUITER", "ADMIN")
                
                // HM endpoints
                .requestMatchers("/api/hm/**").hasAnyRole("HM", "ADMIN")
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Import/Export endpoints
                .requestMatchers("/api/import/**").hasAnyRole("RECRUITER", "ADMIN")
                .requestMatchers("/api/export/**").hasAnyRole("RECRUITER", "ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}) // Use HTTP Basic for simplicity in MVP
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        
        authManagerBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
        
        return authManagerBuilder.build();
    }
}
