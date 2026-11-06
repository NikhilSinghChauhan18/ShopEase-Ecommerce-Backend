
package com.example.E_Commerce.repository;

import com.example.E_Commerce.entity.Order;
import com.example.E_Commerce.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Customer orders
    List<Order> findByUser(User user);

    List<Order> findByUserId(Long userId);

    // Admin: Get all orders
    List<Order> findAllByOrderByOrderDateDesc();

    // Admin: Filter orders by status
    List<Order> findByOrderStatusIgnoreCaseOrderByOrderDateDesc(
            String orderStatus
    );

    // Count orders by status
    long countByOrderStatusIn(Collection<String> orderStatuses);

    // Total revenue from paid orders
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE UPPER(o.paymentStatus) = 'PAID'
    """)
    BigDecimal findTotalPaidRevenue();

    // Latest 5 orders
    List<Order> findTop5ByOrderByOrderDateDesc();
}
