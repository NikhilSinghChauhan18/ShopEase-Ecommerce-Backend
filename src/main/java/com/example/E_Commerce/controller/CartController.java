package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.CartRequest;
import com.example.E_Commerce.dto.CartResponse;
import com.example.E_Commerce.repository.UserRepository;
import com.example.E_Commerce.service.CartService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    // Constructor Injection
    public CartController(
            CartService cartService,
            UserRepository userRepository) {

        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    // Get current authenticated user's ID
    private Long getCurrentUserId(Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"))
                .getId();
    }

    // 1. Add product to cart
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody CartRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        CartResponse response =
                cartService.addToCart(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. Get user's cart
    @GetMapping
    public ResponseEntity<List<CartResponse>> getCart(
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }

    // 3. Remove product from cart
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long productId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        cartService.removeFromCart(userId, productId);

        return ResponseEntity.ok(
                "Product removed from cart successfully"
        );
    }

    // 4. Clear entire cart
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        cartService.clearCart(userId);

        return ResponseEntity.ok(
                "Cart cleared successfully"
        );
    }
}