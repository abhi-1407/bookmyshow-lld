package com.abhilash.bookmyshow;

import com.abhilash.bookmyshow.domain.Booking;
import com.abhilash.bookmyshow.domain.City;
import com.abhilash.bookmyshow.domain.Movie;
import com.abhilash.bookmyshow.domain.Payment;
import com.abhilash.bookmyshow.domain.Screen;
import com.abhilash.bookmyshow.domain.Seat;
import com.abhilash.bookmyshow.domain.Show;
import com.abhilash.bookmyshow.domain.ShowSeat;
import com.abhilash.bookmyshow.domain.Theatre;
import com.abhilash.bookmyshow.domain.User;
import com.abhilash.bookmyshow.domain.enums.SeatStatus;
import com.abhilash.bookmyshow.domain.enums.SeatType;
import com.abhilash.bookmyshow.repository.BookingRepository;
import com.abhilash.bookmyshow.repository.PaymentRepository;
import com.abhilash.bookmyshow.repository.SeatLockRepository;
import com.abhilash.bookmyshow.repository.ShowSeatRepository;
import com.abhilash.bookmyshow.repository.inmemory.InMemoryBookingRepository;
import com.abhilash.bookmyshow.repository.inmemory.InMemoryPaymentRepository;
import com.abhilash.bookmyshow.repository.inmemory.InMemorySeatLockRepository;
import com.abhilash.bookmyshow.repository.inmemory.InMemoryShowSeatRepository;
import com.abhilash.bookmyshow.service.BookingService;
import com.abhilash.bookmyshow.service.PaymentService;
import com.abhilash.bookmyshow.service.SeatLockService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        SeatLockRepository seatLockRepository = new InMemorySeatLockRepository();
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        PaymentRepository paymentRepository = new InMemoryPaymentRepository();
        ShowSeatRepository showSeatRepository = new InMemoryShowSeatRepository();

        SeatLockService seatLockService = new SeatLockService(seatLockRepository);
        BookingService bookingService = new BookingService(seatLockService, bookingRepository);
        PaymentService paymentService = new PaymentService(seatLockService, paymentRepository, bookingRepository, showSeatRepository);

        City city = new City("city-1", "Bangalore");
        Theatre theatre = new Theatre("theatre-1", "PVR", city);
        Screen screen = new Screen("screen-1", "Screen 1", theatre);
        Movie movie = new Movie("movie-1", "Interstellar");
        Show show = new Show("show-1", movie, screen, LocalDateTime.now().plusHours(2));

        User abhilash = new User("user-1", "Abhilash", "abhilash@example.com");
        User rahul = new User("user-2", "Rahul", "rahul@example.com");

        Seat seatA1 = new Seat("seat-A1", "A1", 12, SeatType.REGULAR, screen);
        Seat seatA2 = new Seat("seat-A2", "A2", 13, SeatType.REGULAR, screen);

        ShowSeat showSeatA1 = new ShowSeat("show-seat-A1", show, seatA1, SeatStatus.AVAILABLE, new BigDecimal("300.00"));
        ShowSeat showSeatA2 = new ShowSeat("show-seat-A2", show, seatA2, SeatStatus.AVAILABLE, new BigDecimal("500.00"));

        showSeatRepository.save(showSeatA1);
        showSeatRepository.save(showSeatA2);

        List<ShowSeat> selectedSeats = List.of(showSeatA1, showSeatA2);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicReference<User> winningUser = new AtomicReference<>();

        Runnable abhilashTask = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                System.out.println("Abhilash is attempting to lock seats A1 and A2");

                seatLockService.lockSeats(abhilash, selectedSeats);
                winningUser.compareAndSet(null, abhilash);

                System.out.println("Abhilash successfully locked seats A1 and A2");
            } catch (Exception exception) {
                System.out.println("Abhilash failed to lock seats A1 and A2: " + exception.getMessage());
            }
        };

        Runnable rahulTask = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                System.out.println("Rahul is attempting to lock seats A1 and A2");

                seatLockService.lockSeats(rahul, selectedSeats);
                winningUser.compareAndSet(null, rahul);

                System.out.println("Rahul successfully locked seats A1 and A2");
            } catch (Exception exception) {
                System.out.println("Rahul failed to lock seats A1 and A2: " + exception.getMessage());
            }
        };

        executorService.submit(abhilashTask);
        executorService.submit(rahulTask);

        readyLatch.await();

        System.out.println();
        System.out.println("Concurrent Booking Simulation");
        System.out.println();
        System.out.println("Abhilash and Rahul will attempt to lock " + "seats A1 and A2 concurrently");
        System.out.println();

        startLatch.countDown();
        executorService.shutdown();

        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }

        User winner = winningUser.get();

        if (winner == null) {
            System.out.println("Neither user acquired the seat locks");
            return;
        }

        System.out.println();
        System.out.println("Lock Result");
        System.out.println();
        System.out.println("Abhilash owns the seat locks: " + seatLockService.validateLocks(abhilash, selectedSeats));
        System.out.println("Rahul owns the seat locks: " + seatLockService.validateLocks(rahul, selectedSeats));
        System.out.println();
        System.out.println("Creating booking for " + winner.getName());

        Booking booking = bookingService.createBooking(winner, show, selectedSeats);

        System.out.println("Booking status: " + booking.getStatus());
        System.out.println();
        System.out.println("Processing payment for " + winner.getName());

        Payment payment = paymentService.processPayment(booking, new BigDecimal("800.00"), true);

        System.out.println();
        System.out.println("Final State");
        System.out.println();
        System.out.println("Payment status: " + payment.getStatus());
        System.out.println("Booking status: " + booking.getStatus());
        System.out.println("A1 status: " + showSeatA1.getStatus());
        System.out.println("A2 status: " + showSeatA2.getStatus());
        System.out.println("Temporary locks still valid: " + seatLockService.validateLocks(winner, selectedSeats));
    }
}