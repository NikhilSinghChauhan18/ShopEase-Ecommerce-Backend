
package com.example.E_Commerce.service;
import com.example.E_Commerce.dto.AdminOrderResponse;
import com.example.E_Commerce.dto.OrderResponse;
import com.example.E_Commerce.entity.*;
import com.example.E_Commerce.repository.CartRepository;
import com.example.E_Commerce.repository.OrderRepository;
import com.example.E_Commerce.repository.ProductRepository;
import com.example.E_Commerce.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // Place Order
    @Transactional
    public OrderResponse placeOrder(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        List<CartItem> cartItems = cartRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("PLACED");

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = productRepository.findById(
                    cartItem.getProduct().getId()
            ).orElseThrow(() ->
                    new IllegalArgumentException("Product not found"));

            BigDecimal price = product.getPrice();

            BigDecimal subtotal = price.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(price);
            orderItem.setSubtotal(subtotal);

            order.getOrderItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteByUserId(userId);

        return convertToResponse(savedOrder);
    }

    // Get All Orders of Logged-in User
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return orderRepository.findByUser(user)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get Order by ID
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to access this order"
            );
        }

        return convertToResponse(order);
    }

    // Cancel Order
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to cancel this order"
            );
        }

        if (!"PLACED".equals(order.getOrderStatus())) {
            throw new IllegalArgumentException(
                    "Only placed orders can be cancelled"
            );
        }

        order.setOrderStatus("CANCELLED");
        orderRepository.save(order);
    }


    // ADMIN: Get all orders
    @Transactional(readOnly = true)
    public List<AdminOrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .stream()
                .map(this::convertToAdminResponse)
                .toList();
    }

    // ADMIN: Get orders by status
    @Transactional(readOnly = true)
    public List<AdminOrderResponse> getOrdersByStatus(String status) {
        return orderRepository
                .findByOrderStatusIgnoreCaseOrderByOrderDateDesc(status)
                .stream()
                .map(this::convertToAdminResponse)
                .toList();
    }

    // ADMIN: Get order details
    @Transactional(readOnly = true)
    public AdminOrderResponse getAdminOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        return convertToAdminResponse(order);
    }

    // ADMIN: Update order status
    @Transactional
    public AdminOrderResponse updateOrderStatus(
            Long orderId, String newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        List<String> allowedStatuses = List.of(
                "PLACED",
                "PROCESSING",
                "SHIPPED",
                "DELIVERED",
                "CANCELLED"
        );

        String status = newStatus.toUpperCase().trim();

        if (!allowedStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid order status");
        }

        order.setOrderStatus(status);

        Order updatedOrder = orderRepository.save(order);

        return convertToAdminResponse(updatedOrder);
    }

    // Convert Order Entity to Admin DTO
    private AdminOrderResponse convertToAdminResponse(Order order) {

        List<AdminOrderResponse.OrderItemResponse> items =
                order.getOrderItems()
                        .stream()
                        .map(item ->
                                new AdminOrderResponse.OrderItemResponse(
                                        item.getProduct().getId(),
                                        item.getProduct().getName(),
                                        item.getQuantity(),
                                        item.getPrice(),
                                        item.getSubtotal()
                                ))
                        .toList();

        return new AdminOrderResponse(
                order.getId(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getOrderDate(),
                items
        );
    }

    // Convert Entity to DTO
    private OrderResponse convertToResponse(Order order) {

        List<OrderResponse.OrderItemResponse> items =
                order.getOrderItems()
                        .stream()
                        .map(item ->
                                new OrderResponse.OrderItemResponse(
                                        item.getProduct().getId(),
                                        item.getProduct().getName(),
                                        item.getQuantity(),
                                        item.getPrice(),
                                        item.getSubtotal()
                                ))
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getOrderDate(),
                items
        );
    }
    // ADMIN: Get all orders of a particular customer
    @Transactional(readOnly = true)
    public List<AdminOrderResponse> getCustomerOrders(Long customerId) {

        User customer = userRepository.findById(customerId)
                .filter(user -> "USER".equalsIgnoreCase(user.getRole()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found"));

        return orderRepository.findByUser(customer)
                .stream()
                .map(this::convertToAdminResponse)
                .toList();
    }
}
