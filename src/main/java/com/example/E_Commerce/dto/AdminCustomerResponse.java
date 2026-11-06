package com.example.E_Commerce.dto;

import java.time.LocalDateTime;

public class AdminCustomerResponse {

    private Long customerId;
    private String name;
    private String email;
    private String role;
    private boolean emailVerified;
    private LocalDateTime createdAt;

    public AdminCustomerResponse(
            Long customerId,
            String name,
            String email,
            String role,
            boolean emailVerified,
            LocalDateTime createdAt) {

        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
    }

    public Long getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}