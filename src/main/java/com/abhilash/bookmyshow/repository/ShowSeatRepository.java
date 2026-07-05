package com.abhilash.bookmyshow.repository;

import com.abhilash.bookmyshow.domain.ShowSeat;

import java.util.List;
import java.util.Optional;

public interface ShowSeatRepository {

    ShowSeat save(ShowSeat showSeat);

    Optional<ShowSeat> findById(String showSeatId);

    List<ShowSeat> findByShow(String showId);

    List<ShowSeat> findAvailableSeatsByShow(String showId);
}