package com.abhilash.bookmyshow.domain;

public class Theatre {
    private final String id;
    private final String name;
    private final City city;
    public Theatre(String id, String name, City city) {
        this.id = id;
        this.name = name;
        this.city = city;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public City getCity() {
        return city;
    }
}