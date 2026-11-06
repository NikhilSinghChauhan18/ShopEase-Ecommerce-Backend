
package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.UserRequest;
import com.example.E_Commerce.dto.UserResponse;
import com.example.E_Commerce.entity.User;
import com.example.E_Commerce.repository.UserRepository;
import com.example.E_Commerce.dto.LoginRequest;
import com.example.E_Commerce.dto.LoginResponse;
import com.example.E_Commerce.dto.AdminCustomerResponse;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailOtpService emailOtpService;
    private final EmailService emailService;

    // Constructor Injection
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailOtpService emailOtpService,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailOtpService = emailOtpService;
        this.emailService = emailService;
    }

    // 1. Register User and Send OTP
    public UserResponse registerUser(UserRequest request) {

        User existingUser = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (existingUser != null) {

            if (existingUser.isEmailVerified()) {
                throw new IllegalArgumentException(
                        "Email is already registered"
                );
            }

            emailOtpService.generateOtpForUnverifiedUser(
                    existingUser.getEmail()
            );

            return new UserResponse(
                    existingUser.getId(),
                    existingUser.getName(),
                    existingUser.getEmail(),
                    existingUser.getRole(),
                    existingUser.getCreatedAt()
            );
        }
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Generate and send email verification OTP
        emailOtpService.generateAndSendOtp(savedUser.getEmail());

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
    }

    // 2. Verify Email OTP
    public String verifyEmailOtp(String email, String otp) {

        boolean verified = emailOtpService.verifyOtp(email, otp);

        if (!verified) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        user.setEmailVerified(true);

        userRepository.save(user);

        return "Email verified successfully";
    }

    // 3. Login User with JWT and Send Confirmation Email
    public LoginResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        )
                );

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        // Check email verification
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException(
                    "Please verify your email before logging in"
            );
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail());

        // Send successful login notification
        emailService.sendLoginSuccessEmail(
                user.getEmail(),
                user.getName()
        );

        return new LoginResponse(
                "Login successful",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }
    // Get all customers
    public List<AdminCustomerResponse> getAllCustomers() {

        return userRepository
                .findByRoleIgnoreCaseOrderByCreatedAtDesc("USER")
                .stream()
                .map(user -> new AdminCustomerResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.isEmailVerified(),
                        user.getCreatedAt()
                ))
                .toList();
    }

    // Get customer by ID
    public AdminCustomerResponse getCustomerById(Long id) {

        User user = userRepository.findById(id)
                .filter(u -> "USER".equalsIgnoreCase(u.getRole()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found")
                );

        return new AdminCustomerResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }
}
