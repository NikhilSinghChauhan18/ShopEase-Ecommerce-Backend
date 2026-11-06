
package com.example.E_Commerce.dto;

import java.math.BigDecimal;

public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private BigDecimal amount;
    private String currency;
    private String paymentStatus;

    public PaymentResponse(Long paymentId, Long orderId,
                           String razorpayOrderId,
                           String razorpayKeyId,
                           BigDecimal amount,
                           String currency,
                           String paymentStatus) {

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayKeyId = razorpayKeyId;
        this.amount = amount;
        this.currency = currency;
        this.paymentStatus = paymentStatus;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
