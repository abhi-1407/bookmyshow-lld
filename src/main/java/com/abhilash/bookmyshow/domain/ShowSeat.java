package com.abhilash.bookmyshow.domain;

import com.abhilash.bookmyshow.domain.enums.SeatStatus;
import java.math.BigDecimal;

public class ShowSeat {
    private final String id;
    private final Show show;
    private final Seat seat;
    private SeatStatus status;
    private final BigDecimal price;
    public ShowSeat(String id, Show show, Seat seat, SeatStatus status, BigDecimal price) {
        this.id = id;
        this.show = show;
        this.seat = seat;
        this.status = status;
        this.price = price;
    }
    public String getId() {
        return id;
    }
    public Show getShow() {
        return show;
    }
    public Seat getSeat() {
        return seat;
    }
    public SeatStatus getStatus() {
        return status;
    }
    public void setStatus(SeatStatus status) {
        this.status = status;
    }
    public BigDecimal getPrice() {
        return price;
    }
}