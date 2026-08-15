package com.cinebook.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

public class TheaterMapDto {
    private Long theaterId;
    private String theaterName;
    private String location;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private Integer totalSeats;
    private List<MovieLite> movies;

    public static class MovieLite {
        public String title;
        public String language;
        public MovieLite(String title, String language) { this.title = title; this.language = language; }
    }

    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    public List<MovieLite> getMovies() { return movies; }
    public void setMovies(List<MovieLite> movies) { this.movies = movies; }
}
