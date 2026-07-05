package com.abhilash.bookmyshow.domain;

public class Screen {
    private final String id;
    private final String name;
    private final Theatre theatre;
    public Screen(String id, String name, Theatre theatre) {
        this.id = id;
        this.name = name;
        this.theatre = theatre;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Theatre getTheatre() {
        return theatre;
    }
}