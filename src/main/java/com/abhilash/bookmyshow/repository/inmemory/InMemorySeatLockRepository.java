package com.abhilash.bookmyshow.repository.inmemory;

import com.abhilash.bookmyshow.domain.SeatLock;
import com.abhilash.bookmyshow.repository.SeatLockRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySeatLockRepository implements SeatLockRepository {
    private final Map<String, SeatLock> seatLocks = new ConcurrentHashMap<>();
    @Override
    public SeatLock save(SeatLock seatLock) {
        seatLocks.put(seatLock.getShowSeat().getId(), seatLock);
        return seatLock;
    }
    @Override
    public Optional<SeatLock> findByShowSeatId(String showSeatId) {
        return Optional.ofNullable(seatLocks.get(showSeatId));
    }
    @Override
    public void deleteByShowSeatId(String showSeatId) {
        seatLocks.remove(showSeatId);
    }
}