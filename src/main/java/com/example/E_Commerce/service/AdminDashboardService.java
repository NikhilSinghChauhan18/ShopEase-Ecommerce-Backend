
package com.example.E_Commerce.service;

import com.example.E_Commerce.repository.UserRepository;
import com.example.E_Commerce.repository.ProductRepository;
import com.example.E_Commerce.repository.OrderRepository;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository) {

        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Map<String, Object> getDashboardData() {

        Map<String, Object> dashboard = new LinkedHashMap<>();

        // Total Revenue from Paid Orders
        dashboard.put(
                "totalRevenue",
                orderRepository.findTotalPaidRevenue()
        );

        // Total Orders
        dashboard.put(
                "totalOrders",
                orderRepository.count()
        );

        // Total Products
        dashboard.put(
                "totalProducts",
                productRepository.count()
        );

        // Total Customers (USER role only)
        dashboard.put(
                "totalCustomers",
                userRepository.countByRoleIgnoreCase("USER")
        );

        // Pending Orders
        dashboard.put(
                "pendingOrders",
                orderRepository.countByOrderStatusIn(
                        List.of("PLACED", "PENDING")
                )
        );

        // Recent 5 Orders
        List<Map<String, Object>> recentOrders =
                orderRepository.findTop5ByOrderByOrderDateDesc()
                        .stream()
                        .map(order -> {

                            Map<String, Object> item =
                                    new LinkedHashMap<>();

                            item.put("orderId", order.getId());
                            item.put(
                                    "customerName",
                                    order.getUser().getName()
                            );
                            item.put(
                                    "totalAmount",
                                    order.getTotalAmount()
                            );
                            item.put(
                                    "orderStatus",
                                    order.getOrderStatus()
                            );
                            item.put(
                                    "paymentStatus",
                                    order.getPaymentStatus()
                            );
                            item.put(
                                    "orderDate",
                                    order.getOrderDate()
                            );

                            return item;
                        })
                        .toList();

        dashboard.put("recentOrders", recentOrders);

        return dashboard;
    }
}
