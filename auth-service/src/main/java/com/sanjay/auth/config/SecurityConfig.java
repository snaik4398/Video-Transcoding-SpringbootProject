package com.sanjay.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    // Optional: Set security.enabled=false in application.properties to disable authentication
    @Value("${security.enabled:true}")
    private boolean securityEnabled;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.and())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        // Option to disable authentication entirely
        if (!securityEnabled) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
        
        // Normal security configuration
        http.authorizeHttpRequests(auth -> auth
            // PUBLIC ENDPOINTS (No authentication required)
            // Add new public endpoints here
            .requestMatchers(
                "/api/auth/register", 
                "/api/auth/login", 
                "/api/auth/health",
                "/actuator/**"
                // Add more public endpoints here:
                // "/api/auth/public-info",
                // "/api/auth/api-docs"
            ).permitAll()
            
            // ALL OTHER ENDPOINTS REQUIRE AUTHENTICATION
            // New endpoints you add will automatically be protected
            .anyRequest().authenticated()
        );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
