
package com.example.E_Commerce.service;

import com.example.E_Commerce.dto.PaymentResponse;
import com.example.E_Commerce.dto.PaymentVerifyRequest;
import com.example.E_Commerce.entity.Payment;
import com.example.E_Commerce.repository.OrderRepository;
import com.example.E_Commerce.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;

    private final String razorpayKeyId;
    private final String razorpayKeySecret;

    public PaymentService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret
    ) throws Exception {

        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;

        this.razorpayKeyId = keyId;
        this.razorpayKeySecret = keySecret;

        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    // CREATE RAZORPAY PAYMENT ORDER
    @Transactional
    public PaymentResponse createPayment(Long userId, Long orderId)
            throws Exception {

        // Fetch ShopEase order
        com.example.E_Commerce.entity.Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Order not found"));

        // Verify order ownership
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to pay for this order");
        }

        // Verify order status
        if (!"PLACED".equals(order.getOrderStatus())) {
            throw new IllegalArgumentException(
                    "Only placed orders can be paid");
        }

        // Get amount from database
        BigDecimal amount = order.getTotalAmount();

        // Convert INR to paise
        long amountInPaise = amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        // Create Razorpay order
        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", "order_" + orderId);

        com.razorpay.Order razorpayOrder =
                razorpayClient.orders.create(options);

        // Save payment details
        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setAmount(amount);
        payment.setPaymentStatus("CREATED");

        Payment savedPayment = paymentRepository.save(payment);

        // Return payment response
        return new PaymentResponse(
                savedPayment.getId(),
                order.getId(),
                savedPayment.getRazorpayOrderId(),
                razorpayKeyId,
                amount,
                "INR",
                savedPayment.getPaymentStatus()
        );
    }

    // CREATE RAZORPAY UPI QR CODE
    @Transactional
    public Map<String, Object> createUpiQrPayment(Long userId, Long orderId)
            throws Exception {

        // Fetch ShopEase order
        com.example.E_Commerce.entity.Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Order not found"));

        // Verify order ownership
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to pay for this order");
        }

        // Verify order status
        if (!"PLACED".equals(order.getOrderStatus())) {
            throw new IllegalArgumentException(
                    "Only placed orders can be paid");
        }

        // Prevent creating another QR after successful payment
        if ("PAID".equals(order.getPaymentStatus())) {
            throw new IllegalArgumentException(
                    "This order has already been paid");
        }

        // Get amount from database
        BigDecimal amount = order.getTotalAmount();

        // Convert INR to paise
        long amountInPaise = amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        // QR expiry: 30 minutes from creation
        long closeBy = Instant.now().plusSeconds(1800).getEpochSecond();

        // Prepare Razorpay QR request
        JSONObject options = new JSONObject();

        options.put("type", "upi_qr");
        options.put("name", "ShopEase Order " + orderId);
        options.put("usage", "single_use");
        options.put("fixed_amount", true);
        options.put("payment_amount", amountInPaise);
        options.put("description", "ShopEase order payment " + orderId);
        options.put("close_by", closeBy);

        JSONObject notes = new JSONObject();
        notes.put("order_id", String.valueOf(orderId));
        notes.put("user_id", String.valueOf(userId));

        options.put("notes", notes);

        // Create QR using Razorpay SDK
        com.razorpay.QrCode qrCode =
                razorpayClient.qrCode.create(options);

        // Save QR details in database
        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentStatus("CREATED");

        payment.setRazorpayQrCodeId(qrCode.get("id"));
        payment.setQrImageUrl(qrCode.get("image_url"));
        payment.setQrStatus(qrCode.get("status"));

        payment.setQrExpiresAt(
                LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(closeBy),
                        java.time.ZoneId.systemDefault()
                )
        );

        Payment savedPayment = paymentRepository.save(payment);

        // Return QR details to frontend
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("paymentId", savedPayment.getId());
        response.put("orderId", order.getId());
        response.put("qrCodeId", savedPayment.getRazorpayQrCodeId());
        response.put("qrImageUrl", savedPayment.getQrImageUrl());
        response.put("amount", amount);
        response.put("amountInPaise", amountInPaise);
        response.put("currency", "INR");
        response.put("paymentStatus", savedPayment.getPaymentStatus());
        response.put("qrStatus", savedPayment.getQrStatus());
        response.put("expiresAt", savedPayment.getQrExpiresAt());

        return response;
    }

    // VERIFY RAZORPAY PAYMENT
    @Transactional
    public String verifyPayment(
            Long userId,
            PaymentVerifyRequest request
    ) throws Exception {

        // Find payment using Razorpay Order ID
        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        // Verify order ownership
        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to verify this payment");
        }

        // Prevent duplicate verification
        if ("SUCCESS".equals(payment.getPaymentStatus())) {
            throw new IllegalArgumentException(
                    "Payment has already been verified");
        }

        // Verify that the payment belongs to this order
        if (!"PLACED".equals(payment.getOrder().getOrderStatus())) {
            throw new IllegalArgumentException(
                    "This order is not eligible for payment");
        }

        // Prepare signature verification data
        JSONObject attributes = new JSONObject();

        attributes.put(
                "razorpay_order_id",
                request.getRazorpayOrderId()
        );

        attributes.put(
                "razorpay_payment_id",
                request.getRazorpayPaymentId()
        );

        attributes.put(
                "razorpay_signature",
                request.getRazorpaySignature()
        );

        // Verify Razorpay signature
        boolean isValid = Utils.verifyPaymentSignature(
                attributes,
                razorpayKeySecret
        );

        if (!isValid) {
            throw new IllegalArgumentException(
                    "Invalid payment signature");
        }

        // Fetch actual payment details from Razorpay
        com.razorpay.Payment razorpayPayment =
                razorpayClient.payments.fetch(
                        request.getRazorpayPaymentId()
                );

        String status = razorpayPayment.get("status");

        Long paidAmount = ((Number)
                razorpayPayment.get("amount")).longValue();

        String razorpayOrderId =
                razorpayPayment.get("order_id");

        // Calculate expected amount in paise
        long expectedAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        // Verify payment status, amount and order ID
        if (!"captured".equals(status)
                || paidAmount != expectedAmount
                || !request.getRazorpayOrderId().equals(razorpayOrderId)) {

            throw new IllegalArgumentException(
                    "Payment verification failed: payment details do not match");
        }

        // Save successful payment details
        payment.setRazorpayPaymentId(
                request.getRazorpayPaymentId()
        );

        payment.setRazorpaySignature(
                request.getRazorpaySignature()
        );

        payment.setPaymentStatus("SUCCESS");

        paymentRepository.save(payment);

        // Update ShopEase order payment status
        payment.getOrder().setPaymentStatus("PAID");

        orderRepository.save(payment.getOrder());

        return "Payment verified successfully";
    }
}
