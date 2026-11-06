
package com.example.E_Commerce.config;

import com.example.E_Commerce.filter.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Public root endpoint
                        .requestMatchers("/", "/error").permitAll()

                        // Public APIs
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/verify-otp",
                                "/api/users/resend-otp"
                        ).permitAll()

                        // Admin-only APIs
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Product APIs - View
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // Product APIs - Add, Update, Delete
                        .requestMatchers("/api/products/**")
                        .hasRole("ADMIN")

                        // Category APIs - View
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categories",
                                "/api/categories/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // Category APIs - Add, Update, Delete
                        .requestMatchers(
                                "/api/categories",
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        // Shopping Cart
                        .requestMatchers("/api/cart/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Orders
                        .requestMatchers("/api/orders/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Payments
                        .requestMatchers("/api/payments/**")
                        .hasAnyRole("USER", "ADMIN")

                        // All remaining requests require authentication
                        .requestMatchers("/", "/error").permitAll()
                );

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
