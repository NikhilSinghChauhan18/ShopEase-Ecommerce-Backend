
package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.PaymentRequest;
import com.example.E_Commerce.dto.PaymentResponse;
import com.example.E_Commerce.dto.PaymentVerifyRequest;

import com.example.E_Commerce.entity.User;

import com.example.E_Commerce.repository.UserRepository;
import com.example.E_Commerce.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentController(
            PaymentService paymentService,
            UserRepository userRepository
    ) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    // CREATE RAZORPAY PAYMENT ORDER - CARD CHECKOUT
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication
    ) throws Exception {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        PaymentResponse response = paymentService.createPayment(
                user.getId(),
                request.getOrderId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // CREATE UPI QR CODE
    @PostMapping("/create-qr")
    public ResponseEntity<Map<String, Object>> createUpiQrPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication
    ) throws Exception {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Map<String, Object> response =
                paymentService.createUpiQrPayment(
                        user.getId(),
                        request.getOrderId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // VERIFY RAZORPAY PAYMENT - CARD CHECKOUT
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request,
            Authentication authentication
    ) throws Exception {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        String message = paymentService.verifyPayment(
                user.getId(),
                request
        );

        return ResponseEntity.ok(
                Map.of("message", message)
        );
    }
}
