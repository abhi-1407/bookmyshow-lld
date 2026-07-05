package com.abhilash.bookmyshow.service;

import com.abhilash.bookmyshow.domain.SeatLock;
import com.abhilash.bookmyshow.domain.ShowSeat;
import com.abhilash.bookmyshow.domain.User;
import com.abhilash.bookmyshow.repository.SeatLockRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SeatLockService {

    SeatLockService(SeatLockRepository seatLockRepository){
        this.seatLockRepository = seatLockRepository;
    }
    private SeatLockRepository seatLockRepository;
    public synchronized void lockSeats(User user, List<ShowSeat> showSeatList){
        for (ShowSeat showSeat : showSeatList) {
            Optional<SeatLock> existingLock = seatLockRepository.findByShowSeatId(showSeat.getId());
            if (existingLock.isPresent() && !existingLock.get().isExpired()) {
                throw new IllegalStateException("Seat is already locked: " + showSeat.getId());
            }
            SeatLock seatLock = new SeatLock(UUID.randomUUID().toString(), showSeat, user, LocalDateTime.now());
            seatLockRepository.save(seatLock);
        }

    };
    public void unlockSeat(User user, List<ShowSeat> showSeatList){
        for (ShowSeat showSeat : showSeatList) {
            Optional<SeatLock> existingLock = seatLockRepository.findByShowSeatId(showSeat.getId());
            if (existingLock.isPresent() && existingLock.get().getLockedBy().getId().equals(user.getId())) {
                seatLockRepository.deleteByShowSeatId(showSeat.getId());
            }
        }
    };
    public boolean validateLocks(User user,List<ShowSeat> showSeatList){
        for (ShowSeat showSeat : showSeatList) {
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
    };

}
