package com.abhilash.bookmyshow.domain;

import java.time.LocalDateTime;

public class Show {
    private final String id;
    private final Movie movie;
    private final Screen screen;
    private final LocalDateTime startTime;
    public Show(String id, Movie movie, Screen screen, LocalDateTime startTime) {
        this.id = id;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
    }
    public String getId() {
        return id;
    }
    public Movie getMovie() {
        return movie;
    }
    public Screen getScreen() {
        return screen;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
}