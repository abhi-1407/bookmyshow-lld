package com.abhilash.bookmyshow.domain;

import com.abhilash.bookmyshow.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private final String id;
    private final Booking booking;
    private final BigDecimal amount;
    private PaymentStatus status;
    private final LocalDateTime paymentTime;

    public Payment(String id, Booking booking, BigDecimal amount, PaymentStatus status, LocalDateTime paymentTime) {
        this.id = id;
        this.booking = booking;
        this.amount = amount;
        this.status = status;
        this.paymentTime = paymentTime;
    }

    public String getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
}