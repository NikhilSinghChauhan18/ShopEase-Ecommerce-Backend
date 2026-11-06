
package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.UserRequest;
import com.example.E_Commerce.dto.UserResponse;
import com.example.E_Commerce.dto.LoginRequest;
import com.example.E_Commerce.dto.LoginResponse;
import com.example.E_Commerce.service.UserService;
import com.example.E_Commerce.service.EmailOtpService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final EmailOtpService emailOtpService;

    public UserController(
            UserService userService,
            EmailOtpService emailOtpService) {

        this.userService = userService;
        this.emailOtpService = emailOtpService;
    }

    // Register User
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRequest request) {

        UserResponse response =
                userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Verify Email OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || otp == null ||
                !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$") ||
                !otp.matches("\\d{6}")) {

            throw new IllegalArgumentException(
                    "Valid email and 6-digit OTP are required"
            );
        }

        String message =
                userService.verifyEmailOtp(email, otp);

        return ResponseEntity.ok(
                Map.of("message", message)
        );
    }

    // Resend OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");

        if (email == null ||
                !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new IllegalArgumentException(
                    "Valid email is required"
            );
        }

        emailOtpService.resendOtp(email);

        return ResponseEntity.ok(
                Map.of("message", "OTP sent successfully")
        );
    }
    // Login User
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                userService.loginUser(request);

        return ResponseEntity.ok(response);
    }
}
