package com.student_smart_pay.student_management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for Mobile APIs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll() // Open Login/Register
                .requestMatchers("/api/v1/gate/**").hasAnyRole("GUARD", "CAMPUS_ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/v1/student/**").hasAnyRole("STUDENT", "CAMPUS_ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/v1/campus/**").hasAnyRole("CAMPUS_ADMIN", "SUPER_ADMIN")

                .anyRequest().authenticated()                   // Lock everything else (like Gate/Scanner)
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No Sessions (JWT is Stateless)
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Check Token BEFORE checking password

        return http.build();
    }
}