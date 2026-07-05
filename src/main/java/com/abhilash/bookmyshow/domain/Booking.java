package com.abhilash.bookmyshow.domain;

import com.abhilash.bookmyshow.domain.enums.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;

public class Booking {

    private final String id;
    private final User user;
    private final Show show;
    private final List<ShowSeat> showSeats;
    private final LocalDateTime bookingTime;
    private BookingStatus status;
    public Booking(String id, User user, Show show, List<ShowSeat> showSeats, LocalDateTime bookingTime, BookingStatus status) {
        this.id = id;
        this.user = user;
        this.show = show;
        this.showSeats = showSeats;
        this.bookingTime = bookingTime;
        this.status = status;
    }
    public String getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public Show getShow() {
        return show;
    }
    public List<ShowSeat> getShowSeats() {
        return showSeats;
    }
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    public BookingStatus getStatus() {
        return status;
    }
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}