package com.cinebook.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Everything the seat picker (and booking-service, server-side) needs for one showtime. */
public class ShowtimeInfoDto {
    private Long showtimeId;
    private Long movieId;
    private String movieTitle;
    private String showDate;
    private String showTime;
    private String screen;
    private Long theaterId;
    private String theaterName;
    private String location;
    private Integer totalSeats;
    private Object layout;
    private Map<String, CatPrice> catPrices;

    public static class CatPrice {
        public BigDecimal price;
        public String color;
        public CatPrice(BigDecimal price, String color) { this.price = price; this.color = color; }
    }

    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    public Object getLayout() { return layout; }
    public void setLayout(Object layout) { this.layout = layout; }
    public Map<String, CatPrice> getCatPrices() { return catPrices; }
    public void setCatPrices(Map<String, CatPrice> catPrices) { this.catPrices = catPrices; }
}