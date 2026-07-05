package com.abhilash.bookmyshow.domain;

import com.abhilash.bookmyshow.domain.enums.SeatType;
public class Seat {
    private final String id;
    private final String row;
    private final int seatNumber;
    private final SeatType seatType;
    private final Screen screen;
    public Seat(String id, String row, int seatNumber, SeatType seatType, Screen screen) {
        this.id = id;
        this.row = row;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.screen = screen;
    }
    public String getId() {
        return id;
    }
    public String getRow() {
        return row;
    }
    public int getSeatNumber() {
        return seatNumber;
    }
    public SeatType getSeatType() {
        return seatType;
    }
    public Screen getScreen() {
        return screen;
    }
}