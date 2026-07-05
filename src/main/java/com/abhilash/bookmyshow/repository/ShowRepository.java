package com.abhilash.bookmyshow.repository;

import com.abhilash.bookmyshow.domain.Show;

import java.util.List;
import java.util.Optional;

public interface ShowRepository {

    Show save(Show show);

    Optional<Show> findById(String showId);

    List<Show> findByCity(String cityId);

    List<Show> findByMovieAndCity(String movieId, String cityId);
}