
package com.example.E_Commerce.filter;

import com.example.E_Commerce.entity.User;
import com.example.E_Commerce.repository.UserRepository;
import com.example.E_Commerce.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // Skip JWT authentication for public endpoints
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/users/register")
                || path.equals("/api/users/login")
                || path.equals("/api/users/verify-otp")
                || path.equals("/api/users/resend-otp");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        System.out.println("\n========== JWT AUTHENTICATION DEBUG ==========");
        System.out.println("Request URL: " + request.getRequestURI());

        // Check Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            System.out.println("JWT STATUS: Authorization token missing");

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        try {

            // Extract email from JWT
            String email = jwtService.extractEmail(token);

            System.out.println("JWT EMAIL: " + email);

            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                // Fetch user from database
                User user = userRepository.findByEmail(email).orElse(null);

                System.out.println("USER FOUND: " + (user != null));

                if (user != null) {

                    System.out.println("DATABASE ROLE: " + user.getRole());

                    // Validate JWT
                    boolean tokenValid =
                            jwtService.isTokenValid(token, email);

                    System.out.println("TOKEN VALID: " + tokenValid);

                    if (tokenValid) {

                        // Normalize role to avoid ROLE_ROLE_USER
                        String role = user.getRole()
                                .trim()
                                .toUpperCase(Locale.ROOT);

                        String authority = role.startsWith("ROLE_")
                                ? role
                                : "ROLE_" + role;

                        System.out.println("GRANTED AUTHORITY: " + authority);

                        var authorities = List.of(
                                new SimpleGrantedAuthority(authority)
                        );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        authorities
                                );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);

                        System.out.println("JWT AUTHENTICATION SUCCESS");

                    } else {

                        System.out.println("JWT AUTHENTICATION FAILED: Invalid token");
                        SecurityContextHolder.clearContext();
                    }

                } else {

                    System.out.println("JWT AUTHENTICATION FAILED: User not found");
                    SecurityContextHolder.clearContext();
                }

            } else if (email == null) {

                System.out.println("JWT AUTHENTICATION FAILED: Email is null");
            }

        } catch (JwtException | IllegalArgumentException ex) {

            System.out.println("JWT ERROR: " + ex.getMessage());

            SecurityContextHolder.clearContext();
        }

        System.out.println("===============================================\n");

        filterChain.doFilter(request, response);
    }
}
