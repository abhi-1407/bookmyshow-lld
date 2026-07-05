package com.abhilash.bookmyshow.service;

import com.abhilash.bookmyshow.domain.Booking;
import com.abhilash.bookmyshow.domain.Payment;
import com.abhilash.bookmyshow.domain.ShowSeat;
import com.abhilash.bookmyshow.domain.enums.BookingStatus;
import com.abhilash.bookmyshow.domain.enums.PaymentStatus;
import com.abhilash.bookmyshow.domain.enums.SeatStatus;
import com.abhilash.bookmyshow.repository.BookingRepository;
import com.abhilash.bookmyshow.repository.PaymentRepository;
import com.abhilash.bookmyshow.repository.ShowSeatRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentService {
    private final SeatLockService seatLockService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;

    public PaymentService(SeatLockService seatLockService, PaymentRepository paymentRepository, BookingRepository bookingRepository, ShowSeatRepository showSeatRepository){
        this.seatLockService = seatLockService;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.showSeatRepository = showSeatRepository;
    }

    public Payment processPayment(Booking booking, BigDecimal amount, boolean paymentSuccessful){
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Payment can only be processed for pending bookings");
        }

        Payment payment = new Payment(UUID.randomUUID().toString(),booking,amount, PaymentStatus.PENDING, LocalDateTime.now());
        paymentRepository.save(payment);

        if(paymentSuccessful){
            handlePaymentSuccess(booking,payment);
        }else{
            handlePaymentFailure(booking,payment);
        }

        return payment;
    }
    private void handlePaymentSuccess(Booking booking, Payment payment){
        payment.setStatus(PaymentStatus.SUCCESS);
        booking.setStatus(BookingStatus.CONFIRMED);
        for(ShowSeat showSeat : booking.getShowSeats()){
            showSeat.setStatus(SeatStatus.BOOKED);
            showSeatRepository.save(showSeat);
        }
        seatLockService.unlockSeat(booking.getUser(),booking.getShowSeats());
        paymentRepository.save(payment);
        bookingRepository.save(booking);

    };
    private void handlePaymentFailure(Booking booking, Payment payment){
        payment.setStatus(PaymentStatus.FAILED);
        booking.setStatus(BookingStatus.CANCELLED);
        for(ShowSeat showSeat : booking.getShowSeats()){
            showSeat.setStatus(SeatStatus.AVAILABLE);
            showSeatRepository.save(showSeat);
        }
        seatLockService.unlockSeat(booking.getUser(),booking.getShowSeats());
        paymentRepository.save(payment);
        bookingRepository.save(booking);
    };



}
