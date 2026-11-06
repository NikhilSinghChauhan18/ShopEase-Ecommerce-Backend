
package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.OrderResponse;
import com.example.E_Commerce.repository.UserRepository;
import com.example.E_Commerce.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService,
                           UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    // Get logged-in user's ID from JWT authentication
    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"))
                .getId();
    }

    // Place Order
    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        OrderResponse response = orderService.placeOrder(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get My Orders
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }

    // Get Order By ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        return ResponseEntity.ok(
                orderService.getOrderById(userId, orderId)
        );
    }

    // Cancel Order
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        orderService.cancelOrder(userId, orderId);

        return ResponseEntity.ok("Order cancelled successfully");
    }
}
