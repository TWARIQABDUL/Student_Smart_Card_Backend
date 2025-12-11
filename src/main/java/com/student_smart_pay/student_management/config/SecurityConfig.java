package com.student_smart_pay.student_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Critical: Disable CSRF for Mobile Apps
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/payment/**").permitAll() // Open the Payment API
                .anyRequest().authenticated() // Lock everything else
            );
        
        return http.build();
    }
}