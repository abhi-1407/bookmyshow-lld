// InMemoryShowSeatRepository.java
package com.abhilash.bookmyshow.repository.inmemory;

import com.abhilash.bookmyshow.domain.ShowSeat;
import com.abhilash.bookmyshow.domain.enums.SeatStatus;
import com.abhilash.bookmyshow.repository.ShowSeatRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryShowSeatRepository implements ShowSeatRepository {

    private final Map<String, ShowSeat> showSeats = new ConcurrentHashMap<>();

    @Override
    public ShowSeat save(ShowSeat showSeat) {
        showSeats.put(showSeat.getId(), showSeat);
        return showSeat;
    }

    @Override
    public Optional<ShowSeat> findById(String showSeatId) {
        return Optional.ofNullable(showSeats.get(showSeatId));
    }

    @Override
    public List<ShowSeat> findByShow(String showId) {
        return showSeats.values().stream().filter(showSeat -> showSeat.getShow().getId().equals(showId)).toList();
    }

    @Override
    public List<ShowSeat> findAvailableSeatsByShow(String showId) {
        return showSeats.values().stream().filter(showSeat -> showSeat.getShow().getId().equals(showId)).filter(showSeat -> showSeat.getStatus() == SeatStatus.AVAILABLE).toList();
    }
}