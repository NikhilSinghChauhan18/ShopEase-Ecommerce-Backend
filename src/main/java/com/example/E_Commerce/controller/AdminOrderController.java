
package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.AdminOrderResponse;
import com.example.E_Commerce.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<List<AdminOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // Filter orders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AdminOrderResponse>> getOrdersByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                orderService.getOrdersByStatus(status)
        );
    }

    // Get order details by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderResponse> getOrderById(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getAdminOrderById(orderId)
        );
    }

    // Update order status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<AdminOrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, status)
        );
    }
}
