package com.abhilash.bookmyshow.repository;

import com.abhilash.bookmyshow.domain.Booking;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(String bookingId);
}