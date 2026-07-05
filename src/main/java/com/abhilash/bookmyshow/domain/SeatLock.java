package com.abhilash.bookmyshow.domain;

import java.time.LocalDateTime;

public class SeatLock {
    private static final int LOCK_DURATION_MINUTES = 5;
    private final String id;
    private final ShowSeat showSeat;
    private final User lockedBy;
    private final LocalDateTime lockStartTime;
    public SeatLock(String id, ShowSeat showSeat, User lockedBy, LocalDateTime lockStartTime) {
        this.id = id;
        this.showSeat = showSeat;
        this.lockedBy = lockedBy;
        this.lockStartTime = lockStartTime;
    }
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(lockStartTime.plusMinutes(LOCK_DURATION_MINUTES));
    }
    public String getId() {
        return id;
    }
    public ShowSeat getShowSeat() {
        return showSeat;
    }
    public User getLockedBy() {
        return lockedBy;
    }
    public LocalDateTime getLockStartTime() {
        return lockStartTime;
    }
}