package com.abhilash.bookmyshow.repository;

import com.abhilash.bookmyshow.domain.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(String paymentId);
    List<Payment> findByBookingId(String bookingId);
}