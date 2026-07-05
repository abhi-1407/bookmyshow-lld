package com.abhilash.bookmyshow.repository.inmemory;

import com.abhilash.bookmyshow.domain.Payment;
import com.abhilash.bookmyshow.repository.PaymentRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        payments.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    @Override
    public List<Payment> findByBookingId(String bookingId) {
        return payments.values().stream().filter(payment -> payment.getBooking().getId().equals(bookingId)).toList();
    }
}