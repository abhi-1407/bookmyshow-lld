package com.abhilash.bookmyshow.service;

import com.abhilash.bookmyshow.domain.Booking;
import com.abhilash.bookmyshow.domain.Show;
import com.abhilash.bookmyshow.domain.ShowSeat;
import com.abhilash.bookmyshow.domain.User;
import com.abhilash.bookmyshow.domain.enums.BookingStatus;
import com.abhilash.bookmyshow.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingService {

    private final SeatLockService seatLockService;
    private final BookingRepository bookingRepository;
    public BookingService(SeatLockService seatLockService, BookingRepository bookingRepository){
        this.seatLockService = seatLockService;
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(User user, Show show, List<ShowSeat> showSeatList){
        validateSeatsBelongToShow(show,showSeatList);

        if (!seatLockService.validateLocks(user, showSeatList)) {
            throw new IllegalStateException("User does not own valid locks for selected seats");
        }
        Booking booking = new Booking(UUID.randomUUID().toString(), user, show, showSeatList, LocalDateTime.now(), BookingStatus.PENDING);
        return bookingRepository.save(booking);
    }

    private void validateSeatsBelongToShow(Show show, List<ShowSeat> showSeats) {
        boolean invalidSeatExists = showSeats.stream().anyMatch(showSeat -> !showSeat.getShow().getId().equals(show.getId()));
        if (invalidSeatExists) {
            throw new IllegalArgumentException("All seats must belong to the same show");
        }
    }

}
