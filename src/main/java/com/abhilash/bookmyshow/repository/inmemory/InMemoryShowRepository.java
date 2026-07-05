package com.abhilash.bookmyshow.repository.inmemory;

import com.abhilash.bookmyshow.domain.Show;
import com.abhilash.bookmyshow.repository.ShowRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryShowRepository implements ShowRepository {

    private final Map<String, Show> shows = new ConcurrentHashMap<>();

    @Override
    public Show save(Show show) {
        shows.put(show.getId(), show);
        return show;
    }

    @Override
    public Optional<Show> findById(String showId) {
        return Optional.ofNullable(shows.get(showId));
    }

    @Override
    public List<Show> findByCity(String cityId) {
        return shows.values().stream().filter(show -> show.getScreen().getTheatre().getCity().getId().equals(cityId)).toList();
    }

    @Override
    public List<Show> findByMovieAndCity(String movieId, String cityId) {
        return shows.values().stream().filter(show -> show.getMovie().getId().equals(movieId)).filter(show -> show.getScreen().getTheatre().getCity().getId().equals(cityId)).toList();
    }
}