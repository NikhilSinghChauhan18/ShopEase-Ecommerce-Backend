package com.example.E_Commerce.controller;

import com.example.E_Commerce.dto.AdminCustomerResponse;
import com.example.E_Commerce.dto.AdminOrderResponse;
import com.example.E_Commerce.service.UserService;
import com.example.E_Commerce.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final UserService userService;
    private final OrderService orderService;

    public AdminCustomerController(
            UserService userService,
            OrderService orderService) {

        this.userService = userService;
        this.orderService = orderService;
    }

    // Get all customers
    @GetMapping
    public ResponseEntity<List<AdminCustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(userService.getAllCustomers());
    }

    // Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<AdminCustomerResponse> getCustomerById(
            @PathVariable Long id) {

        return ResponseEntity.ok(userService.getCustomerById(id));
    }

    // Get customer order history
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<AdminOrderResponse>> getCustomerOrders(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.getCustomerOrders(id)
        );
    }
}