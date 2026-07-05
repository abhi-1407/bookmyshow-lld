package com.abhilash.bookmyshow.repository;

import com.abhilash.bookmyshow.domain.SeatLock;
import java.util.Optional;

public interface SeatLockRepository {
    SeatLock save(SeatLock seatLock);
    Optional<SeatLock> findByShowSeatId(String showSeatId);
    void deleteByShowSeatId(String showSeatId);
}