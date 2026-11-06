
package com.example.E_Commerce.repository;

import com.example.E_Commerce.entity.Payment;
import com.example.E_Commerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    Optional<Payment> findByRazorpayQrCodeId(String razorpayQrCodeId);

    List<Payment> findByOrder(Order order);
}
