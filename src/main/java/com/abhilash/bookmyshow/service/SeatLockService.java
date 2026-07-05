package com.abhilash.bookmyshow.service;

import com.abhilash.bookmyshow.domain.SeatLock;
import com.abhilash.bookmyshow.domain.ShowSeat;
import com.abhilash.bookmyshow.domain.User;
import com.abhilash.bookmyshow.repository.SeatLockRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SeatLockService {
    private final SeatLockRepository seatLockRepository;

    // Maintains one ReentrantLock per ShowSeat to allow concurrent operations
    // on different seats while preventing concurrent modification of the same seat.
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public SeatLockService(SeatLockRepository seatLockRepository){
        this.seatLockRepository = seatLockRepository;
    }
    public void lockSeats(User user, List<ShowSeat> showSeatList){

        // Sort seats to ensure consistent lock ordering across threads
        // and prevent deadlocks when multiple seat locks are acquired.
        List<ShowSeat> sortedSeats = new ArrayList<>(showSeatList);
        sortedSeats.sort(Comparator.comparing(ShowSeat::getId));
        List<ReentrantLock> acquiredLocks = new ArrayList<>();

        for (ShowSeat showSeat : sortedSeats) {
            ReentrantLock lock = locks.computeIfAbsent(showSeat.getId(), id -> new ReentrantLock());
            acquiredLocks.add(lock);
        }

        for (ReentrantLock lock : acquiredLocks) {
            lock.lock();
        }

        try {
            for (ShowSeat showSeat : sortedSeats) {
                Optional<SeatLock> existingLock = seatLockRepository.findByShowSeatId(showSeat.getId());
                if (existingLock.isPresent() && !existingLock.get().isExpired()) {
                    throw new IllegalStateException("Seat is already locked: " + showSeat.getId());
                }
            }
            for (ShowSeat showSeat : sortedSeats) {
                SeatLock seatLock = new SeatLock(UUID.randomUUID().toString(), showSeat, user, LocalDateTime.now());
                seatLockRepository.save(seatLock);
            }
        }finally{
            for(int i = acquiredLocks.size() - 1; i >= 0; i--){
                // We never delete the locks as another thread which is already acquiring a lock would get access
                // At the same time since we have deleted the entry another thread can take access to the same CS
                // Now there are two threads holding lock to one section
                acquiredLocks.get(i).unlock();
            }
        }
    }
    public void unlockSeats(User user, List<ShowSeat> showSeatList){
        List<ShowSeat> sortedSeats = new ArrayList<>(showSeatList);
        sortedSeats.sort(Comparator.comparing(ShowSeat::getId));
        List<ReentrantLock> acquiredLocks = new ArrayList<>();

        for (ShowSeat showSeat : sortedSeats) {
            ReentrantLock lock = locks.computeIfAbsent(showSeat.getId(), id -> new ReentrantLock());
            acquiredLocks.add(lock);
        }
        for (ReentrantLock lock : acquiredLocks) {
            lock.lock();
        }

        try{
        for (ShowSeat showSeat : sortedSeats) {
            Optional<SeatLock> existingLock = seatLockRepository.findByShowSeatId(showSeat.getId());
            if (existingLock.isPresent() && existingLock.get().getLockedBy().getId().equals(user.getId())) {
                seatLockRepository.deleteByShowSeatId(showSeat.getId());
            }
        }}finally {
            for(int i = acquiredLocks.size() - 1; i >= 0; i--){
                acquiredLocks.get(i).unlock();
            }
        }
    }
    public boolean validateLocks(User user,List<ShowSeat> showSeatList){

        List<ShowSeat> sortedSeats = new ArrayList<>(showSeatList);
        sortedSeats.sort(Comparator.comparing(ShowSeat::getId));

        List<ReentrantLock> acquiredLocks = new ArrayList<>();

        for (ShowSeat showSeat : sortedSeats) {
            ReentrantLock lock = locks.computeIfAbsent(showSeat.getId(), id -> new ReentrantLock());
            acquiredLocks.add(lock);
        }

        for (ReentrantLock lock : acquiredLocks) {
            lock.lock();
        }

        try {
            for (ShowSeat showSeat : sortedSeats) {
                Optional<SeatLock> lock = seatLockRepository.findByShowSeatId(showSeat.getId());
                if (lock.isEmpty()) {
                    return false;
                }
                if (lock.get().isExpired()) {
                    return false;
                }
                if (!lock.get().getLockedBy().getId().equals(user.getId())) {
                    return false;
                }
            }
            return true;
        }finally{
            for(int i = acquiredLocks.size() - 1; i >= 0; i--){
                acquiredLocks.get(i).unlock();
            }
        }
    }
}
